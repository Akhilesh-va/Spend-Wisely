package com.example.mindfullexpenses.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "daily_limit_minor") val dailyLimitMinor: Long,
    @ColumnInfo(name = "currency_code") val currencyCode: String = "INR",
    @ColumnInfo(name = "reset_hour") val resetHour: Int = 0,
    @ColumnInfo(name = "mode") val mode: BudgetMode = BudgetMode.DAILY,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant = Instant.now()
)

enum class BudgetMode {
    DAILY,
    WEEKLY,
    MONTHLY
}


