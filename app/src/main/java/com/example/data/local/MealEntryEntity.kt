package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.MealType

@Entity(
    tableName = "meal_entries",
    indices = [
        Index(value = ["date"]),
        Index(value = ["mealType"]),
        Index(value = ["foodId"])
    ]
)
data class MealEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format "YYYY-MM-DD"
    val mealType: MealType,
    val foodId: Long,
    val foodName: String,
    val foodBrand: String = "",
    val quantityGrams: Double,
    val caloriesPer100g: Double,
    val totalCalories: Double = (caloriesPer100g * quantityGrams / 100.0),
    val proteins: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0,
    val salt: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val unit: String = "g",
    val unitQuantity: Double = quantityGrams
)
