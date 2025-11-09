package com.example.mindfullexpenses.core.autotrack

import com.example.mindfullexpenses.core.autotrack.parser.NotificationParser
import com.example.mindfullexpenses.core.autotrack.parser.ParsedExpense
import com.example.mindfullexpenses.core.autotrack.parser.SmsParser
import com.example.mindfullexpenses.core.di.IoDispatcher
import com.example.mindfullexpenses.core.model.Expense
import com.example.mindfullexpenses.core.model.ExpenseCategory
import com.example.mindfullexpenses.core.model.ExpenseSource
import com.example.mindfullexpenses.core.repository.DuplicateExpenseException
import com.example.mindfullexpenses.core.repository.ExpenseRepository
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class NotificationIngestionManager @Inject constructor(
    private val parser: NotificationParser,
    private val smsParser: SmsParser,
    private val categorizationEngine: AutoCategorizationEngine,
    private val repository: ExpenseRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun ingest(
        packageName: String,
        title: CharSequence?,
        text: CharSequence?,
        timestamp: Long,
        notificationId: String?
    ): IngestionResult = withContext(ioDispatcher) {
        val parsed = when {
            NotificationSources.smsPackages.contains(packageName) -> smsParser.parse(
                address = title?.toString(),
                body = text?.toString(),
                timestampMillis = timestamp
            )

            else -> parser.parse(
                packageName = packageName,
                title = title,
                text = text,
                timestamp = timestamp
            )
        } ?: return@withContext IngestionResult.Unhandled

        persistParsedExpense(parsed, notificationId)
    }

    suspend fun ingestParsed(
        parsed: ParsedExpense,
        notificationId: String? = null
    ): IngestionResult = withContext(ioDispatcher) {
        persistParsedExpense(parsed, notificationId)
    }

    private suspend fun persistParsedExpense(
        parsed: ParsedExpense,
        notificationId: String?
    ): IngestionResult {
        val category = categorizationEngine.categorize(parsed.cleanMerchant)
        val expense = Expense(
            amountMinor = parsed.amountMinor,
            currencyCode = parsed.currencyCode,
            merchant = parsed.rawMerchant,
            cleanMerchant = parsed.cleanMerchant,
            category = category,
            paymentMethod = parsed.paymentMethod,
            timestamp = parsed.timestamp,
            source = if (parsed.isCredit) ExpenseSource.REFUND else ExpenseSource.AUTO,
            notificationId = notificationId
        )

        return repository.logExpense(expense).fold(
            onSuccess = { id ->
                IngestionResult.Success(expense.copy(id = id))
            },
            onFailure = { error ->
                when (error) {
                    is DuplicateExpenseException -> IngestionResult.Duplicate(existingId = error.existingId)
                    else -> IngestionResult.Failure(error)
                }
            }
        )
    }
}

sealed interface IngestionResult {
    data class Success(val expense: Expense) : IngestionResult
    data class Duplicate(val existingId: Long) : IngestionResult
    data class Failure(val throwable: Throwable) : IngestionResult
    data object Unhandled : IngestionResult
}


