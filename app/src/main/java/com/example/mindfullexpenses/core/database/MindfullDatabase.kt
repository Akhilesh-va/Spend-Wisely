package com.example.mindfullexpenses.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.mindfullexpenses.core.database.dao.BudgetDao
import com.example.mindfullexpenses.core.database.dao.ExpenseDao
import com.example.mindfullexpenses.core.database.dao.MerchantCategoryDao
import com.example.mindfullexpenses.core.database.entity.BudgetEntity
import com.example.mindfullexpenses.core.database.entity.ExpenseEntity
import com.example.mindfullexpenses.core.database.entity.MerchantCategoryMappingEntity

@Database(
    entities = [
        ExpenseEntity::class,
        BudgetEntity::class,
        MerchantCategoryMappingEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class MindfullDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun merchantCategoryDao(): MerchantCategoryDao
}


