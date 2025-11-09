package com.example.mindfullexpenses.ui.screens.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindfullexpenses.R
import com.example.mindfullexpenses.core.database.entity.BudgetEntity
import com.example.mindfullexpenses.core.database.entity.BudgetMode
import com.example.mindfullexpenses.core.model.Expense
import com.example.mindfullexpenses.core.model.ExpenseSource
import com.example.mindfullexpenses.core.notification.NotificationAccessManager
import com.example.mindfullexpenses.core.repository.ExpenseRepository
import com.example.mindfullexpenses.core.util.CurrencyFormatter
import com.example.mindfullexpenses.core.util.TimeRange
import com.example.mindfullexpenses.core.util.TimeRangeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlin.math.max
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val notificationAccessManager: NotificationAccessManager
) : ViewModel() {

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private val timeFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault())

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DashboardEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<DashboardEvent> = _events.asSharedFlow()

    private var latestBudget: BudgetEntity? = null
    private val dayRange = MutableStateFlow(TimeRangeProvider.today())
    private var dayRefreshJob: Job? = null
    private var currentResetHour: Int = DEFAULT_RESET_HOUR

    init {
        scheduleDayRefresh(currentResetHour)
        refreshNotificationAccess()
        observeDashboard()
    }

    fun onEnableAutoTrackingClicked() {
        Log.i(TAG, "Enable auto-tracking pressed")
        viewModelScope.launch {
            _events.emit(DashboardEvent.RequestEnableAutoTracking)
        }
    }

    fun onAdjustBudgetClicked() {
        Log.i(TAG, "Adjust budget pressed")
        val currentLimit = _uiState.value.budgetLimitMinor
        _uiState.update { state ->
            state.copy(
                showBudgetDialog = true,
                budgetInput = formatMinorToInput(currentLimit),
                budgetInputError = null
            )
        }
    }

    fun onBudgetDialogDismissed() {
        _uiState.update { state ->
            state.copy(
                showBudgetDialog = false,
                budgetInputError = null,
                isUpdatingBudget = false
            )
        }
    }

    fun onBudgetInputChanged(value: String) {
        val sanitized = value.filter { it.isDigit() || it == '.' }
        _uiState.update { state ->
            state.copy(budgetInput = sanitized, budgetInputError = null)
        }
    }

    fun onBudgetSaveConfirmed() {
        val input = _uiState.value.budgetInput
        val amountMinor = parseBudgetInput(input)
        if (amountMinor == null || amountMinor <= 0L) {
            _uiState.update { state ->
                state.copy(budgetInputError = INVALID_BUDGET_INPUT_MESSAGE)
            }
            viewModelScope.launch {
                _events.emit(DashboardEvent.BudgetUpdateFailed("Invalid amount"))
            }
            return
        }

        _uiState.update { it.copy(isUpdatingBudget = true, budgetInputError = null) }
        viewModelScope.launch {
            try {
                val existing = latestBudget
                val entity = (existing?.copy(
                    dailyLimitMinor = amountMinor,
                    updatedAt = Instant.now()
                ) ?: BudgetEntity(
                    id = 1,
                    dailyLimitMinor = amountMinor,
                    currencyCode = existing?.currencyCode ?: "INR",
                    resetHour = existing?.resetHour ?: 0,
                    mode = existing?.mode ?: BudgetMode.DAILY
                ))
                repository.upsertBudget(entity)
                latestBudget = entity
                _uiState.update { state ->
                    state.copy(
                        showBudgetDialog = false,
                        isUpdatingBudget = false,
                        budgetInputError = null
                    )
                }
                _events.emit(DashboardEvent.BudgetUpdated)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to update budget", t)
                _uiState.update { state ->
                    state.copy(
                        isUpdatingBudget = false,
                        budgetInputError = t.message ?: "Unknown error"
                    )
                }
                _events.emit(DashboardEvent.BudgetUpdateFailed(t.message ?: "Unknown error"))
            }
        }
    }

    fun onDeleteManualExpenseRequested(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(id)
                _events.emit(DashboardEvent.ManualExpenseDeleted)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to delete manual expense", t)
                _events.emit(DashboardEvent.ManualExpenseDeleteFailed(t.message ?: "Unknown error"))
            }
        }
    }

    fun refreshNotificationAccess() {
        val granted = notificationAccessManager.hasNotificationAccess()
        Log.d(TAG, "Notification access granted=$granted")
        _uiState.update { it.copy(hasNotificationAccess = granted) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDashboard() {
        viewModelScope.launch {
            dayRange.flatMapLatest { range ->
                combine(
                    repository.observeBudget(),
                    repository.observeExpenses(range),
                    repository.observeSpendTotal(range)
                ) { budget, expenses, totalMinor ->
                    DashboardSnapshot(range, budget, expenses, totalMinor)
                }
            }.collect { snapshot ->
                latestBudget = snapshot.budget
                ensureDayRefresh(snapshot.range, snapshot.budget)
                _uiState.update { previous ->
                    mapToUiState(snapshot.budget, snapshot.expenses, snapshot.totalMinor, previous)
                }
            }
        }
    }

    private fun ensureDayRefresh(currentRange: TimeRange, budget: BudgetEntity?) {
        val resetHour = budget?.resetHour ?: DEFAULT_RESET_HOUR
        if (currentResetHour != resetHour) {
            currentResetHour = resetHour
            val desiredRange = TimeRangeProvider.today(resetHour)
            if (dayRange.value != desiredRange) {
                dayRange.value = desiredRange
            }
            scheduleDayRefresh(resetHour)
        } else if (dayRange.value != currentRange) {
            dayRange.value = currentRange
        }
    }

    private fun scheduleDayRefresh(resetHour: Int) {
        dayRefreshJob?.cancel()
        dayRefreshJob = viewModelScope.launch {
            while (isActive) {
                val nextBoundary = TimeRangeProvider.nextDailyBoundary(resetHour)
                val delayMillis = max(0L, nextBoundary.toEpochMilli() - Instant.now().toEpochMilli())
                if (delayMillis > 0L) {
                    delay(delayMillis)
                } else {
                    delay(1_000L)
                }
                val updatedRange = TimeRangeProvider.today(resetHour)
                if (dayRange.value != updatedRange) {
                    dayRange.value = updatedRange
                }
            }
        }
    }

    private fun mapToUiState(
        budget: BudgetEntity?,
        expenses: List<Expense>,
        totalMinor: Long,
        previousState: DashboardUiState
    ): DashboardUiState {
        val limitMinor = budget?.dailyLimitMinor ?: ExpenseRepository.DEFAULT_DAILY_LIMIT_MINOR
        val currencyCode = budget?.currencyCode ?: "INR"
        val remainingMinor = (limitMinor - totalMinor).coerceAtLeast(0L)
        val progress = if (limitMinor == 0L) 0f else (totalMinor / limitMinor.toFloat()).coerceIn(0f, 1f)

        val budgetProgress = BudgetProgressUi(
            spentLabel = CurrencyFormatter.formatMinor(totalMinor, currencyCode),
            limitLabel = CurrencyFormatter.formatMinor(limitMinor, currencyCode),
            remainingLabel = CurrencyFormatter.formatMinor(remainingMinor, currencyCode),
            progress = progress
        )

        val timelineItems = expenses
            .sortedByDescending { it.timestamp }
            .take(10)
            .map { expense ->
                ExpenseTimelineItemUi(
                    id = expense.id,
                    merchant = expense.merchant,
                    amountLabel = CurrencyFormatter.formatMinor(expense.amountMinor, expense.currencyCode),
                    category = expense.category.displayName,
                    time = timeFormatter.format(expense.timestamp.atZone(zoneId)),
                    source = expense.source.toUi()
                )
            }

        val autoTrackedCount = expenses.count { it.source == ExpenseSource.AUTO }
        val budgetInput = if (previousState.showBudgetDialog) {
            previousState.budgetInput
        } else {
            formatMinorToInput(limitMinor)
        }
        val budgetError = if (previousState.showBudgetDialog) previousState.budgetInputError else null

        return previousState.copy(
            isTrackingEnabled = autoTrackedCount > 0,
            trackedCountToday = autoTrackedCount,
            budgetProgress = budgetProgress,
            manualEntryHintRes = R.string.manual_entry_prompt,
            recentExpenses = timelineItems,
            budgetLimitMinor = limitMinor,
            budgetInput = budgetInput,
            budgetInputError = budgetError,
            hasNotificationAccess = previousState.hasNotificationAccess
        )
    }

    private fun ExpenseSource.toUi(): ExpenseSourceUi = when (this) {
        ExpenseSource.AUTO -> ExpenseSourceUi.AutoTracked
        ExpenseSource.MANUAL -> ExpenseSourceUi.Manual
        ExpenseSource.REFUND -> ExpenseSourceUi.Refund
        ExpenseSource.ADJUSTMENT -> ExpenseSourceUi.Manual
    }

    private fun formatMinorToInput(minor: Long): String {
        return String.format(Locale.getDefault(), "%.2f", minor / 100.0)
    }

    private fun parseBudgetInput(input: String): Long? {
        if (input.isBlank()) return null
        val normalized = input.replace(',', '.').trim()
        return runCatching {
            BigDecimal(normalized)
                .setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
                .longValueExact()
        }.getOrNull()
    }

    companion object {
        private const val TAG = "DashboardViewModel"
        private const val INVALID_BUDGET_INPUT_MESSAGE = "Enter a valid amount greater than zero."
        private const val DEFAULT_RESET_HOUR = 0
    }
}

private data class DashboardSnapshot(
    val range: TimeRange,
    val budget: BudgetEntity?,
    val expenses: List<Expense>,
    val totalMinor: Long
)

sealed interface DashboardEvent {
    data object RequestEnableAutoTracking : DashboardEvent
    data object BudgetUpdated : DashboardEvent
    data class BudgetUpdateFailed(val reason: String) : DashboardEvent
    data object ManualExpenseDeleted : DashboardEvent
    data class ManualExpenseDeleteFailed(val reason: String) : DashboardEvent
}


