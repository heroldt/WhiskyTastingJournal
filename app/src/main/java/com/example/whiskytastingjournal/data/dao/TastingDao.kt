package com.example.whiskytastingjournal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.whiskytastingjournal.model.TastingEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface TastingDao {

    @Query("SELECT * FROM tastings ORDER BY date DESC")
    fun getAllTastings(): Flow<List<TastingEntry>>

    @Query("SELECT * FROM tastings WHERE id = :id")
    suspend fun getTastingById(id: String): TastingEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tasting: TastingEntry)

    @Update
    suspend fun update(tasting: TastingEntry)

    @Delete
    suspend fun delete(tasting: TastingEntry)

    @Query("DELETE FROM tastings WHERE id = :id")
    suspend fun deleteById(id: String)
}
