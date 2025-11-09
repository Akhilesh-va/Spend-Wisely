package com.example.mindfullexpenses.core.di

import android.content.Context
import androidx.room.Room
import com.example.mindfullexpenses.core.database.MindfullDatabase
import com.example.mindfullexpenses.core.database.dao.BudgetDao
import com.example.mindfullexpenses.core.database.dao.ExpenseDao
import com.example.mindfullexpenses.core.database.dao.MerchantCategoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "mindfull_expenses.db"

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MindfullDatabase = Room.databaseBuilder(
        context,
        MindfullDatabase::class.java,
        DATABASE_NAME
    )
        .fallbackToDestructiveMigrationOnDowngrade()
        .build()

    @Provides
    fun provideExpenseDao(database: MindfullDatabase): ExpenseDao = database.expenseDao()

    @Provides
    fun provideBudgetDao(database: MindfullDatabase): BudgetDao = database.budgetDao()

    @Provides
    fun provideMerchantCategoryDao(database: MindfullDatabase): MerchantCategoryDao =
        database.merchantCategoryDao()
}


