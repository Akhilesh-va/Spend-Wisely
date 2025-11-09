package com.example.mindfullexpenses.core.database.model

import com.example.mindfullexpenses.core.database.entity.ExpenseEntity
import com.example.mindfullexpenses.core.model.Expense

fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    merchant = merchant,
    cleanMerchant = cleanMerchant,
    category = category,
    paymentMethod = paymentMethod,
    timestamp = timestamp,
    source = source,
    notificationId = notificationId,
    notes = notes
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    amountMinor = amountMinor,
    currencyCode = currencyCode,
    merchant = merchant,
    cleanMerchant = cleanMerchant,
    category = category,
    paymentMethod = paymentMethod,
    timestamp = timestamp,
    source = source,
    notificationId = notificationId,
    notes = notes
)


