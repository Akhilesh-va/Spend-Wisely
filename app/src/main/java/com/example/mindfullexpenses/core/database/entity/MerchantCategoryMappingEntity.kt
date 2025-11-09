package com.example.mindfullexpenses.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mindfullexpenses.core.model.ExpenseCategory

@Entity(tableName = "merchant_category_mappings")
data class MerchantCategoryMappingEntity(
    @PrimaryKey @ColumnInfo(name = "merchant_key") val merchantKey: String,
    @ColumnInfo(name = "category") val category: ExpenseCategory,
    @ColumnInfo(name = "confidence") val confidence: Int = 1
)


