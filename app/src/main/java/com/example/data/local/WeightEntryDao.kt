package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightEntryDao {
    @Query("SELECT * FROM weight_entries ORDER BY date DESC, timestamp DESC")
    fun getAllEntriesDesc(): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries ORDER BY date ASC, timestamp ASC")
    fun getAllEntriesAsc(): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries WHERE date = :date LIMIT 1")
    suspend fun getEntryForDate(date: String): WeightEntryEntity?

    @Query("SELECT * FROM weight_entries ORDER BY date DESC, timestamp DESC LIMIT 1")
    fun getLatestEntry(): Flow<WeightEntryEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: WeightEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: WeightEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: WeightEntryEntity)

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("SELECT * FROM weight_entries ORDER BY date ASC")
    suspend fun getAllEntriesSync(): List<WeightEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<WeightEntryEntity>)

    @Query("DELETE FROM weight_entries")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM weight_entries")
    suspend fun getCount(): Int
}
