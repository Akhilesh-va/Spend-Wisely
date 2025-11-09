package com.example.mindfullexpenses.core.database

import androidx.room.TypeConverter
import com.example.mindfullexpenses.core.database.entity.BudgetMode
import com.example.mindfullexpenses.core.model.ExpenseCategory
import com.example.mindfullexpenses.core.model.ExpenseSource
import java.time.Instant

class Converters {

    @TypeConverter
    fun instantToLong(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun longToInstant(epochMilli: Long?): Instant? =
        epochMilli?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun categoryToString(category: ExpenseCategory?): String? = category?.name

    @TypeConverter
    fun stringToCategory(value: String?): ExpenseCategory? =
        value?.let { runCatching { ExpenseCategory.valueOf(it) }.getOrDefault(ExpenseCategory.OTHER) }

    @TypeConverter
    fun sourceToString(source: ExpenseSource?): String? = source?.name

    @TypeConverter
    fun stringToSource(value: String?): ExpenseSource? =
        value?.let { runCatching { ExpenseSource.valueOf(it) }.getOrDefault(ExpenseSource.AUTO) }

    @TypeConverter
    fun budgetModeToString(mode: BudgetMode?): String? = mode?.name

    @TypeConverter
    fun stringToBudgetMode(value: String?): BudgetMode? =
        value?.let { runCatching { BudgetMode.valueOf(it) }.getOrDefault(BudgetMode.DAILY) }
}


