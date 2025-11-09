package com.example.mindfullexpenses.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.mindfullexpenses.core.model.ExpenseCategory
import com.example.mindfullexpenses.core.model.ExpenseSource
import java.time.Instant

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["merchant"]),
        Index(value = ["category"]),
        Index(value = ["source"])
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "amount_minor") val amountMinor: Long,
    @ColumnInfo(name = "currency_code") val currencyCode: String = "INR",
    @ColumnInfo(name = "merchant") val merchant: String,
    @ColumnInfo(name = "clean_merchant") val cleanMerchant: String,
    @ColumnInfo(name = "category") val category: ExpenseCategory,
    @ColumnInfo(name = "payment_method") val paymentMethod: String,
    @ColumnInfo(name = "timestamp") val timestamp: Instant,
    @ColumnInfo(name = "source") val source: ExpenseSource,
    @ColumnInfo(name = "notification_id") val notificationId: String? = null,
    @ColumnInfo(name = "notes") val notes: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Instant = Instant.now(),
    @ColumnInfo(name = "updated_at") val updatedAt: Instant? = null
)

val ExpenseEntity.amount: Double
    get() = amountMinor / 100.0


