package com.example.mindfullexpenses.ui.screens.manual

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindfullexpenses.core.autotrack.AutoCategorizationEngine
import com.example.mindfullexpenses.core.autotrack.normalizeMerchant
import com.example.mindfullexpenses.core.model.ExpenseCategory
import com.example.mindfullexpenses.core.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ManualEntryViewModel @Inject constructor(
    private val repository: ExpenseRepository,
    private val categorizationEngine: AutoCategorizationEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManualEntryUiState())
    val uiState: StateFlow<ManualEntryUiState> = _uiState.asStateFlow()

    fun onAmountChanged(value: String) {
        val sanitized = value.filter { it.isDigit() || it == '.' }
        _uiState.update { state ->
            val isEnabled = parseAmountToMinor(sanitized) != null && state.selectedCategory != null
            state.copy(
                amount = sanitized,
                isSaveEnabled = isEnabled,
                feedback = ManualEntryFeedback.Idle
            )
        }
    }

    fun onMerchantChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                merchant = value.take(60),
                feedback = ManualEntryFeedback.Idle
            )
        }
    }

    fun onCategorySelected(category: ExpenseCategory) {
        _uiState.update { state ->
            val isEnabled = parseAmountToMinor(state.amount) != null
            state.copy(
                selectedCategory = category,
                isSaveEnabled = isEnabled,
                feedback = ManualEntryFeedback.Idle
            )
        }
    }

    fun onNotesChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                notes = value.take(200),
                feedback = ManualEntryFeedback.Idle
            )
        }
    }

    fun onSaveExpense() {
        val current = _uiState.value
        val amountMinor = parseAmountToMinor(current.amount)
        val category = current.selectedCategory

        if (amountMinor == null || category == null) {
            Log.w(TAG, "Manual save blocked - amount or category missing")
            _uiState.update { state ->
                state.copy(
                    feedback = ManualEntryFeedback.Error("Enter a valid amount and select a category.")
                )
            }
            return
        }

        Log.i(TAG, "Saving manual expense: amountMinor=$amountMinor category=${category.name} merchant='${current.merchant}'")

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, feedback = ManualEntryFeedback.Idle) }
            val merchant = current.merchant.trim()
            val cleanMerchant = merchant.normalizeMerchant()
            val result = repository.addManualExpense(
                amountMinor = amountMinor,
                category = category,
                merchant = merchant,
                cleanMerchant = cleanMerchant,
                paymentMethod = "Manual",
                timestamp = Instant.now(),
                notes = current.notes.ifBlank { null }
            )

            result.fold(
                onSuccess = {
                    categorizationEngine.learn(cleanMerchant, category)
                    Log.i(TAG, "Manual expense saved (id=$it)")
                    _uiState.update { state ->
                        val suggestionLabel = merchant.ifBlank { cleanMerchant }
                        val updatedSuggestions = (listOfNotNull(suggestionLabel.ifBlank { null }) + state.suggestedMerchants)
                            .distinct()
                            .take(SUGGESTED_MERCHANTS_LIMIT)
                            .map {
                                it.replaceFirstChar { char ->
                                    if (char.isLowerCase()) char.titlecase(Locale.getDefault()) else char.toString()
                                }
                            }
                        state.copy(
                            amount = "",
                            merchant = "",
                            notes = "",
                            isSaving = false,
                            isSaveEnabled = false,
                            feedback = ManualEntryFeedback.Success("Expense saved"),
                            suggestedMerchants = updatedSuggestions
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { state ->
                        Log.e(TAG, "Failed to save manual expense", error)
                        state.copy(
                            isSaving = false,
                            feedback = ManualEntryFeedback.Error(
                                error.message ?: "Couldn't save expense"
                            )
                        )
                    }
                }
            )
        }
    }

    private fun parseAmountToMinor(input: String): Long? {
        if (input.isBlank()) return null
        return runCatching {
            BigDecimal(input)
                .setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
                .toLong()
        }.getOrNull()?.takeIf { it > 0L }
    }

    companion object {
        private const val SUGGESTED_MERCHANTS_LIMIT = 6
        private const val TAG = "ManualEntryViewModel"
    }
}

data class ManualEntryUiState(
    val amount: String = "",
    val selectedCategory: ExpenseCategory? = ExpenseCategory.entries.firstOrNull(),
    val merchant: String = "",
    val notes: String = "",
    val availableCategories: List<ExpenseCategory> = ExpenseCategory.entries.toList(),
    val suggestedMerchants: List<String> = emptyList(),
    val isSaveEnabled: Boolean = false,
    val isSaving: Boolean = false,
    val feedback: ManualEntryFeedback = ManualEntryFeedback.Idle
)

sealed interface ManualEntryFeedback {
    data object Idle : ManualEntryFeedback
    data class Success(val message: String) : ManualEntryFeedback
    data class Error(val message: String) : ManualEntryFeedback
}

