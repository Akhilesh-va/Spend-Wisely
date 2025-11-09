package com.example.mindfullexpenses.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mindfullexpenses.core.database.entity.ExpenseEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expenses: List<ExpenseEntity>): List<Long>

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Query(
        """
            SELECT * FROM expenses 
            WHERE timestamp BETWEEN :startInclusive AND :endExclusive
            ORDER BY timestamp DESC
        """
    )
    fun observeExpensesBetween(
        startInclusive: Instant,
        endExclusive: Instant
    ): Flow<List<ExpenseEntity>>

    @Query(
        """
            SELECT * FROM expenses
            WHERE clean_merchant = :merchant
            AND amount_minor = :amountMinor
            AND timestamp BETWEEN :startInclusive AND :endExclusive
        """
    )
    suspend fun findDuplicates(
        merchant: String,
        amountMinor: Long,
        startInclusive: Instant,
        endExclusive: Instant
    ): List<ExpenseEntity>

    @Query(
        """
            SELECT SUM(amount_minor) FROM expenses
            WHERE timestamp BETWEEN :startInclusive AND :endExclusive
            AND source != 'REFUND'
        """
    )
    fun observeSpendTotal(
        startInclusive: Instant,
        endExclusive: Instant
    ): Flow<Long?>

    @Query(
        """
            DELETE FROM expenses
            WHERE id = :id
        """
    )
    suspend fun deleteById(id: Long)
}


