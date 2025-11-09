package com.example.mindfullexpenses.core.repository

import com.example.mindfullexpenses.core.database.dao.BudgetDao
import com.example.mindfullexpenses.core.database.dao.ExpenseDao
import com.example.mindfullexpenses.core.database.dao.MerchantCategoryDao
import com.example.mindfullexpenses.core.database.entity.BudgetEntity
import com.example.mindfullexpenses.core.database.entity.MerchantCategoryMappingEntity
import com.example.mindfullexpenses.core.database.model.toDomain
import com.example.mindfullexpenses.core.database.model.toEntity
import com.example.mindfullexpenses.core.model.Expense
import com.example.mindfullexpenses.core.model.ExpenseCategory
import com.example.mindfullexpenses.core.model.ExpenseSource
import com.example.mindfullexpenses.core.util.TimeRange
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao,
    private val merchantCategoryDao: MerchantCategoryDao
) {

    suspend fun logExpense(
        expense: Expense,
        duplicateWindowMs: Long = DEFAULT_DUPLICATE_WINDOW_MS
    ): Result<Long> = runCatching {
        val windowStart = expense.timestamp.minusMillis(duplicateWindowMs)
        val windowEnd = expense.timestamp.plusMillis(duplicateWindowMs)
        val duplicates = expenseDao.findDuplicates(
            merchant = expense.cleanMerchant,
            amountMinor = expense.amountMinor,
            startInclusive = windowStart,
            endExclusive = windowEnd
        )
        if (duplicates.isNotEmpty()) {
            throw DuplicateExpenseException(duplicates.first().id)
        }
        expenseDao.insert(expense.toEntity())
    }

    fun observeExpenses(
        startInclusive: Instant,
        endExclusive: Instant
    ): Flow<List<Expense>> = expenseDao.observeExpensesBetween(startInclusive, endExclusive)
        .map { entities -> entities.map { it.toDomain() } }

    fun observeExpenses(range: TimeRange): Flow<List<Expense>> =
        observeExpenses(range.start, range.endExclusive)

    fun observeSpendTotal(
        startInclusive: Instant,
        endExclusive: Instant
    ): Flow<Long> = expenseDao.observeSpendTotal(startInclusive, endExclusive)
        .map { it ?: 0L }

    fun observeSpendTotal(range: TimeRange): Flow<Long> =
        observeSpendTotal(range.start, range.endExclusive)

    fun observeBudget(): Flow<BudgetEntity?> = budgetDao.observeBudget()

    suspend fun upsertBudget(entity: BudgetEntity) {
        budgetDao.upsert(entity)
    }

    suspend fun rememberMerchantCategory(merchantKey: String, category: ExpenseCategory) {
        val existing = merchantCategoryDao.findMapping(merchantKey)
        val updated = existing?.copy(
            category = category,
            confidence = (existing.confidence + 1).coerceAtMost(MAX_CONFIDENCE)
        ) ?: MerchantCategoryMappingEntity(
            merchantKey = merchantKey,
            category = category,
            confidence = 1
        )
        merchantCategoryDao.upsert(updated)
    }

    suspend fun resolveCategoryForMerchant(merchantKey: String): ExpenseCategory? {
        return merchantCategoryDao.findMapping(merchantKey)?.category
    }

    suspend fun batchRememberMerchantMappings(mappings: Map<String, ExpenseCategory>) {
        val entities = mappings.map { (merchant, category) ->
            MerchantCategoryMappingEntity(
                merchantKey = merchant,
                category = category,
                confidence = MIN_CONFIDENCE
            )
        }
        merchantCategoryDao.upsertAll(entities)
    }

    suspend fun addManualExpense(
        amountMinor: Long,
        category: ExpenseCategory,
        merchant: String,
        cleanMerchant: String,
        paymentMethod: String,
        timestamp: Instant,
        notes: String?
    ): Result<Long> {
        val expense = Expense(
            amountMinor = amountMinor,
            currencyCode = "INR",
            merchant = merchant.ifBlank { cleanMerchant.ifBlank { "Manual purchase" } },
            cleanMerchant = cleanMerchant.ifBlank { merchant.ifBlank { "manual" } },
            category = category,
            paymentMethod = paymentMethod,
            timestamp = timestamp,
            source = ExpenseSource.MANUAL,
            notes = notes
        )
        return logExpense(expense, duplicateWindowMs = 0)
    }

    suspend fun deleteExpense(id: Long) {
        expenseDao.deleteById(id)
    }

    companion object {
        private const val MAX_CONFIDENCE = 10
        private const val MIN_CONFIDENCE = 1
        private const val DEFAULT_DUPLICATE_WINDOW_MS = 5_000L
        const val DEFAULT_DAILY_LIMIT_MINOR = 100_000L
    }
}

class DuplicateExpenseException(val existingId: Long) : IllegalStateException("Duplicate expense detected: $existingId")


