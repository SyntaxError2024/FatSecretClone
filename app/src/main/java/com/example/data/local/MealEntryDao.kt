package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MealType
import kotlinx.coroutines.flow.Flow

@Dao
interface MealEntryDao {
    @Query("SELECT * FROM meal_entries WHERE date = :date ORDER BY timestamp ASC")
    fun getEntriesForDate(date: String): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM meal_entries WHERE date = :date AND mealType = :mealType ORDER BY timestamp ASC")
    fun getEntriesForDateAndMeal(date: String, mealType: MealType): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM meal_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getEntriesBetweenDates(startDate: String, endDate: String): Flow<List<MealEntryEntity>>

    @Query("SELECT * FROM meal_entries ORDER BY date DESC, timestamp DESC")
    fun getAllEntries(): Flow<List<MealEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: MealEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: MealEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: MealEntryEntity)

    @Query("DELETE FROM meal_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("SELECT DISTINCT date FROM meal_entries ORDER BY date DESC")
    fun getRecordedDates(): Flow<List<String>>

    @Query("SELECT quantityGrams FROM meal_entries WHERE foodId = :foodId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastQuantityForFoodId(foodId: Long): Double?

    @Query("SELECT quantityGrams FROM meal_entries WHERE (foodName = :foodName AND foodBrand = :foodBrand) OR (foodId = :foodId AND :foodId > 0) ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastQuantityForFood(foodId: Long, foodName: String, foodBrand: String): Double?

    @Query("SELECT * FROM meal_entries WHERE (foodName = :foodName AND foodBrand = :foodBrand) OR (foodId = :foodId AND :foodId > 0) ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastEntryForFood(foodId: Long, foodName: String, foodBrand: String): MealEntryEntity?

    @Query("SELECT * FROM meal_entries ORDER BY timestamp ASC")
    suspend fun getAllEntriesSync(): List<MealEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<MealEntryEntity>)

    @Query("SELECT COUNT(*) FROM meal_entries")
    suspend fun getEntriesCount(): Int

    @Query("DELETE FROM meal_entries")
    suspend fun clearAllEntries()
}
