package com.example.mindfullexpenses.core.model

import java.time.Instant

data class Expense(
    val id: Long = 0,
    val amountMinor: Long,
    val currencyCode: String = "INR",
    val merchant: String,
    val cleanMerchant: String,
    val category: ExpenseCategory,
    val paymentMethod: String,
    val timestamp: Instant,
    val source: ExpenseSource,
    val notificationId: String? = null,
    val notes: String? = null
)


