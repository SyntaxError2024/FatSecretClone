package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "weight_entries",
    indices = [Index(value = ["date"], unique = true)]
)
data class WeightEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // ISO date "YYYY-MM-DD"
    val weightKg: Double,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
