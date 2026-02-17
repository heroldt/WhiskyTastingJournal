package com.example.whiskytastingjournal.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.whiskytastingjournal.model.Whisky
import com.example.whiskytastingjournal.model.WhiskyWithTastings
import kotlinx.coroutines.flow.Flow

@Dao
interface WhiskyDao {

    @Query("SELECT * FROM whiskies ORDER BY whiskyName ASC")
    fun getAllWhiskies(): Flow<List<Whisky>>

    @Transaction
    @Query("SELECT * FROM whiskies ORDER BY whiskyName ASC")
    fun getAllWhiskiesWithTastings(): Flow<List<WhiskyWithTastings>>

    @Transaction
    @Query("SELECT * FROM whiskies WHERE id = :id")
    suspend fun getWhiskyWithTastings(id: String): WhiskyWithTastings?

    @Query("SELECT * FROM whiskies WHERE id = :id")
    suspend fun getWhiskyById(id: String): Whisky?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(whisky: Whisky)

    @Update
    suspend fun update(whisky: Whisky)

    @Delete
    suspend fun delete(whisky: Whisky)

    @Query("DELETE FROM whiskies WHERE id = :id")
    suspend fun deleteById(id: String)
}
