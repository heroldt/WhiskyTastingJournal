package com.example.whiskytastingjournal.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.whiskytastingjournal.model.AromaTag
import com.example.whiskytastingjournal.model.AromaTagCount
import com.example.whiskytastingjournal.model.TastingAroma
import kotlinx.coroutines.flow.Flow

@Dao
interface AromaDao {

    @Query("SELECT * FROM aroma_tags ORDER BY category, name")
    fun getAllTags(): Flow<List<AromaTag>>

    @Query("SELECT COUNT(*) FROM aroma_tags")
    suspend fun getTagCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllTags(tags: List<AromaTag>)

    @Query("SELECT * FROM tasting_aromas WHERE tastingId = :tastingId")
    suspend fun getAromasForTasting(tastingId: String): List<TastingAroma>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTastingAroma(tastingAroma: TastingAroma)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTastingAromas(aromas: List<TastingAroma>)

    @Query("DELETE FROM tasting_aromas WHERE tastingId = :tastingId")
    suspend fun clearAllTastingAromas(tastingId: String)

    @Query("SELECT * FROM aroma_tags")
    suspend fun getAllTagsList(): List<AromaTag>

    @Query("SELECT * FROM tasting_aromas")
    suspend fun getAllTastingAromas(): List<TastingAroma>

    @Query("""
        SELECT at.name, at.category, COUNT(*) as count
        FROM tasting_aromas ta
        JOIN aroma_tags at ON ta.aromaId = at.id
        GROUP BY ta.aromaId
        ORDER BY count DESC
        LIMIT 10
    """)
    fun getTopAromaTagCounts(): Flow<List<AromaTagCount>>
}
