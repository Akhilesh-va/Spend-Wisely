package com.example.mindfullexpenses.core.autotrack

import com.example.mindfullexpenses.core.model.ExpenseCategory
import com.example.mindfullexpenses.core.repository.ExpenseRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AutoCategorizationEngine @Inject constructor(
    private val repository: ExpenseRepository
) {

    suspend fun categorize(
        merchant: String,
        fallback: ExpenseCategory = ExpenseCategory.OTHER
    ): ExpenseCategory = withContext(Dispatchers.Default) {
        val normalized = merchant.normalizeMerchant()

        repository.resolveCategoryForMerchant(normalized)?.let { return@withContext it }

        keywordRules.entries.firstOrNull { (_, keywords) ->
            keywords.any { keyword -> normalized.contains(keyword) }
        }?.key ?: fallback
    }

    suspend fun learn(merchant: String, category: ExpenseCategory) {
        val key = merchant.normalizeMerchant()
        repository.rememberMerchantCategory(key, category)
    }

    companion object {
        private val keywordRules: Map<ExpenseCategory, List<String>> = mapOf(
            ExpenseCategory.FOOD_AND_DINING to listOf("zomato", "swiggy", "kfc", "mcdonald", "restaurant", "cafe", "coffee"),
            ExpenseCategory.TRANSPORT to listOf("uber", "ola", "rapido", "metro", "taxi", "fuel", "petrol", "diesel"),
            ExpenseCategory.SHOPPING to listOf("amazon", "flipkart", "myntra", "store", "mall"),
            ExpenseCategory.ENTERTAINMENT to listOf("netflix", "spotify", "prime", "bookmyshow", "movie"),
            ExpenseCategory.BILLS_AND_UTILITIES to listOf("electricity", "water", "gas", "recharge", "bill", "rent"),
            ExpenseCategory.HEALTH to listOf("pharmacy", "apollo", "clinic", "hospital", "med"),
            ExpenseCategory.SUBSCRIPTIONS to listOf("subscription", "monthly", "membership"),
            ExpenseCategory.GROCERIES to listOf("bigbasket", "blinkit", "grofers", "fresh", "mart")
        )
    }
}

fun String.normalizeMerchant(): String = lowercase()
    .replace("upi-", "")
    .replace(Regex("@[a-zA-Z]+"), "")
    .replace(Regex("\\d{10}"), "")
    .replace(Regex("[^a-z0-9\\s]"), " ")
    .replace(Regex("\\s+"), " ")
    .trim()


