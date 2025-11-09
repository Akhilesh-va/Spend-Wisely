package com.example.mindfullexpenses.ui.screens.reports

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindfullexpenses.R
import com.example.mindfullexpenses.core.model.Expense
import com.example.mindfullexpenses.core.model.ExpenseSource
import com.example.mindfullexpenses.core.repository.ExpenseRepository
import com.example.mindfullexpenses.core.util.CurrencyFormatter
import com.example.mindfullexpenses.core.util.TimeRange
import com.example.mindfullexpenses.core.util.TimeRangeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val selectedPeriod = MutableStateFlow(ReportsPeriod.Daily)

    private val _uiState = MutableStateFlow(
        ReportsUiState(
            selectedPeriod = ReportsPeriod.Daily,
            accuracyLabel = "--",
            series = emptyList(),
            highlights = emptyList(),
            categoryBreakdown = emptyList(),
            totalSpendLabel = "₹0",
            manualShare = 0f,
            autoShare = 0f,
            refundCount = 0,
            totalTransactions = 0
        )
    )
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private val percentFormat = NumberFormat.getPercentInstance(Locale.getDefault()).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 0
    }

    init {
        observeReports()
    }

    fun onPeriodSelected(period: ReportsPeriod) {
        if (selectedPeriod.value != period) {
            selectedPeriod.value = period
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeReports() {
        viewModelScope.launch {
            selectedPeriod
                .flatMapLatest { period ->
                    repository.observeExpenses(period.toRange())
                        .map { expenses -> period to expenses }
                }
                .collect { (period, expenses) ->
                    _uiState.value = buildUiState(period, expenses)
                }
        }
    }

    private fun buildUiState(period: ReportsPeriod, expenses: List<Expense>): ReportsUiState {
        val totalMinor = expenses.sumOf { it.amountMinor }
        val totalCount = expenses.size
        val autoCount = expenses.count { it.source == ExpenseSource.AUTO }
        val manualCount = expenses.count { it.source == ExpenseSource.MANUAL }
        val refundCount = expenses.count { it.source == ExpenseSource.REFUND }

        val accuracyLabel = if (totalCount == 0) {
            "--"
        } else {
            val accuracy = autoCount.toDouble() / totalCount.toDouble()
            percentFormat.format(accuracy)
        }

        val categoryBreakdown = buildCategoryBreakdown(expenses, totalMinor)
        val series = buildSeries(period, expenses, totalMinor, manualCount, refundCount)
        val highlights = buildHighlights(expenses, totalMinor, manualCount, refundCount)

        val manualShare = if (totalCount == 0) 0f else manualCount.toFloat() / totalCount
        val autoShare = if (totalCount == 0) 0f else autoCount.toFloat() / totalCount

        return ReportsUiState(
            selectedPeriod = period,
            accuracyLabel = accuracyLabel,
            series = series,
            highlights = highlights,
            categoryBreakdown = categoryBreakdown,
            totalSpendLabel = CurrencyFormatter.formatMinor(totalMinor),
            manualShare = manualShare,
            autoShare = autoShare,
            refundCount = refundCount,
            totalTransactions = totalCount
        )
    }

    private fun buildSeries(
        period: ReportsPeriod,
        expenses: List<Expense>,
        totalMinor: Long,
        manualCount: Int,
        refundCount: Int
    ): List<ReportSeriesUi> {
        if (expenses.isEmpty()) {
            return emptyList()
        }

        val periodTitle = when (period) {
            ReportsPeriod.Daily -> "Today"
            ReportsPeriod.Weekly -> "This week"
            ReportsPeriod.Monthly -> "This month"
        }

        val summarySubtitle = buildString {
            append(CurrencyFormatter.formatMinor(totalMinor))
            append(" across ")
            append(expenses.size)
            append(if (expenses.size == 1) " transaction" else " transactions")
            if (manualCount > 0) {
                append(" • ")
                append(manualCount)
                append(" manual")
            }
            if (refundCount > 0) {
                append(" • ")
                append(refundCount)
                append(" refund")
                if (refundCount > 1) append('s')
            }
        }

        val categorySubtitle = topCategoriesSubtitle(expenses)
        val merchantSubtitle = topMerchantsSubtitle(expenses)

        return listOfNotNull(
            ReportSeriesUi(title = periodTitle, subtitle = summarySubtitle),
            categorySubtitle?.let { ReportSeriesUi(title = "Top categories", subtitle = it) },
            merchantSubtitle?.let { ReportSeriesUi(title = "Top merchants", subtitle = it) }
        )
    }

    private fun buildHighlights(
        expenses: List<Expense>,
        totalMinor: Long,
        manualCount: Int,
        refundCount: Int
    ): List<String> {
        if (expenses.isEmpty()) {
            return emptyList()
        }

        val highlights = mutableListOf<String>()

        val averageTransactionMinor = if (expenses.isNotEmpty()) totalMinor / expenses.size else 0L
        if (averageTransactionMinor > 0) {
            highlights += "Average transaction ${CurrencyFormatter.formatMinor(averageTransactionMinor)}"
        }
        if (manualCount > 0) {
            highlights += "$manualCount manual entr${if (manualCount == 1) "y" else "ies"}"
        }
        if (refundCount > 0) {
            highlights += "$refundCount refund${if (refundCount > 1) "s" else ""} recorded"
        }
        topCategoryHighlight(expenses)?.let(highlights::add)

        return highlights
    }

    private fun buildCategoryBreakdown(
        expenses: List<Expense>,
        totalMinor: Long
    ): List<CategoryBreakdownUi> {
        if (expenses.isEmpty() || totalMinor == 0L) return emptyList()
        return expenses
            .groupBy { it.category.displayName }
            .mapValues { entry -> entry.value.sumOf { it.amountMinor } }
            .entries
            .sortedByDescending { it.value }
            .take(5)
            .map { (category, amount) ->
                val percent = amount.toFloat() / totalMinor.toFloat()
                CategoryBreakdownUi(
                    name = category,
                    percent = percent,
                    amountLabel = CurrencyFormatter.formatMinor(amount),
                    percentLabel = percentFormat.format(percent)
                )
            }
    }

    private fun topCategoriesSubtitle(expenses: List<Expense>): String? {
        if (expenses.isEmpty()) return null
        val totals = expenses.groupBy { it.category.displayName }
            .mapValues { entry -> entry.value.sumOf { it.amountMinor } }
            .entries
            .sortedByDescending { it.value }
            .take(3)

        if (totals.isEmpty()) return null

        return totals.joinToString(" • ") { (category, total) ->
            "$category: ${CurrencyFormatter.formatMinorCompact(total)}"
        }
    }

    private fun topMerchantsSubtitle(expenses: List<Expense>): String? {
        if (expenses.isEmpty()) return null
        val totals = expenses.groupBy { it.merchant }
            .mapValues { entry -> entry.value.sumOf { it.amountMinor } }
            .entries
            .sortedByDescending { it.value }
            .take(3)

        if (totals.isEmpty()) return null

        return totals.joinToString(" • ") { (merchant, total) ->
            "$merchant: ${CurrencyFormatter.formatMinorCompact(total)}"
        }
    }

    private fun topCategoryHighlight(expenses: List<Expense>): String? {
        val top = expenses.groupBy { it.category.displayName }
            .mapValues { entry -> entry.value.sumOf { it.amountMinor } }
            .maxByOrNull { it.value }
            ?: return null

        return "${top.key} leads with ${CurrencyFormatter.formatMinor(top.value)}"
    }
}

data class ReportsUiState(
    val selectedPeriod: ReportsPeriod,
    val accuracyLabel: String,
    val series: List<ReportSeriesUi>,
    val highlights: List<String>,
    val categoryBreakdown: List<CategoryBreakdownUi>,
    val totalSpendLabel: String,
    val manualShare: Float,
    val autoShare: Float,
    val refundCount: Int,
    val totalTransactions: Int
)

data class ReportSeriesUi(
    val title: String,
    val subtitle: String
)

data class CategoryBreakdownUi(
    val name: String,
    val percent: Float,
    val amountLabel: String,
    val percentLabel: String
)

enum class ReportsPeriod(@StringRes val labelRes: Int) {
    Daily(R.string.reports_toggle_daily),
    Weekly(R.string.reports_toggle_weekly),
    Monthly(R.string.reports_toggle_monthly)
}

private fun ReportsPeriod.toRange(): TimeRange = when (this) {
    ReportsPeriod.Daily -> TimeRangeProvider.today()
    ReportsPeriod.Weekly -> TimeRangeProvider.currentWeek()
    ReportsPeriod.Monthly -> TimeRangeProvider.currentMonth()
}

