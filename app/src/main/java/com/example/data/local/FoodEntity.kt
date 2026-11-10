package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "foods",
    indices = [
        Index(value = ["name"]),
        Index(value = ["barcode"])
    ]
)
data class FoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val brand: String = "",
    val caloriesPer100g: Double,
    val proteinsPer100g: Double = 0.0,
    val carbsPer100g: Double = 0.0,
    val fatPer100g: Double = 0.0,
    val fiberPer100g: Double = 0.0,
    val saltPer100g: Double = 0.0,
    val defaultServingGrams: Double = 100.0,
    val servingUnitName: String = "g",
    val barcode: String? = null,
    val isCustom: Boolean = false,
    val lastUsedTimestamp: Long = System.currentTimeMillis(),
    val usageCount: Int = 1
)
