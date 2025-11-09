package com.example.mindfullexpenses.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.mindfullexpenses.core.database.entity.MerchantCategoryMappingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantCategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(mapping: MerchantCategoryMappingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(mappings: List<MerchantCategoryMappingEntity>)

    @Update
    suspend fun update(mapping: MerchantCategoryMappingEntity)

    @Query("SELECT * FROM merchant_category_mappings WHERE merchant_key = :merchant LIMIT 1")
    suspend fun findMapping(merchant: String): MerchantCategoryMappingEntity?

    @Query("SELECT * FROM merchant_category_mappings")
    fun observeMappings(): Flow<List<MerchantCategoryMappingEntity>>
}


