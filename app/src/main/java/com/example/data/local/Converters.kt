package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.MealType

class Converters {
    @TypeConverter
    fun fromMealType(value: MealType): String {
        return value.name
    }

    @TypeConverter
    fun toMealType(value: String): MealType {
        return try {
            MealType.valueOf(value)
        } catch (e: Exception) {
            MealType.SNACK
        }
    }
}
