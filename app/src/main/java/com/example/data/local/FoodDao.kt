package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity): Long

    @Update
    suspend fun updateFood(food: FoodEntity)

    @Query("SELECT * FROM foods WHERE id = :id")
    suspend fun getFoodById(id: Long): FoodEntity?

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' ORDER BY usageCount DESC, lastUsedTimestamp DESC LIMIT 50")
    fun searchFoods(query: String): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE name IS NOT NULL AND TRIM(name) != '' ORDER BY lastUsedTimestamp DESC LIMIT :limit")
    fun getRecentFoods(limit: Int = 50): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE name = :name AND brand = :brand LIMIT 1")
    suspend fun findByNameAndBrand(name: String, brand: String): FoodEntity?

    @Query("SELECT * FROM foods WHERE barcode = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): FoodEntity?

    @Query("UPDATE foods SET usageCount = usageCount + 1, lastUsedTimestamp = :timestamp WHERE id = :foodId")
    suspend fun incrementUsage(foodId: Long, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE foods SET usageCount = usageCount + 1, lastUsedTimestamp = :timestamp, defaultServingGrams = :lastQuantity WHERE id = :foodId")
    suspend fun recordFoodUsage(foodId: Long, lastQuantity: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE foods SET defaultServingGrams = :lastQuantity WHERE id = :foodId")
    suspend fun updateLastQuantity(foodId: Long, lastQuantity: Double)

    @Query("UPDATE foods SET defaultServingGrams = (SELECT quantityGrams FROM meal_entries WHERE meal_entries.foodId = foods.id ORDER BY timestamp DESC LIMIT 1) WHERE EXISTS (SELECT 1 FROM meal_entries WHERE meal_entries.foodId = foods.id)")
    suspend fun syncLastQuantitiesFromMealHistory()

    @Query("DELETE FROM foods WHERE id = :id")
    suspend fun deleteFood(id: Long)

    @Query("SELECT * FROM foods ORDER BY id ASC")
    suspend fun getAllFoodsSync(): List<FoodEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodEntity>)

    @Query("SELECT COUNT(*) FROM foods")
    suspend fun getFoodsCount(): Int

    @Query("DELETE FROM foods")
    suspend fun clearAllFoods()
}
