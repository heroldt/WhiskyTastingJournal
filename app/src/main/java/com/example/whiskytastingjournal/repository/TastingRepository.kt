package com.example.whiskytastingjournal.repository

import com.example.whiskytastingjournal.data.dao.AromaDao
import com.example.whiskytastingjournal.data.dao.TastingDao
import com.example.whiskytastingjournal.data.dao.WhiskyDao
import android.content.Context
import android.net.Uri
import com.example.whiskytastingjournal.model.AromaTag
import com.example.whiskytastingjournal.model.AromaTagCount
import com.example.whiskytastingjournal.model.DefaultAromaTags
import com.example.whiskytastingjournal.util.ExportManager
import com.example.whiskytastingjournal.util.ImportManager
import com.example.whiskytastingjournal.util.ImportResult
import java.io.File
import com.example.whiskytastingjournal.model.TastingAroma
import com.example.whiskytastingjournal.model.TastingEntry
import com.example.whiskytastingjournal.model.Whisky
import com.example.whiskytastingjournal.model.WhiskyWithTastings
import kotlinx.coroutines.flow.Flow

class TastingRepository(
    private val tastingDao: TastingDao,
    private val whiskyDao: WhiskyDao,
    private val aromaDao: AromaDao
) {

    // --- Whisky operations ---
    val allWhiskiesWithTastings: Flow<List<WhiskyWithTastings>> = whiskyDao.getAllWhiskiesWithTastings()

    suspend fun getWhiskyById(id: String): Whisky? = whiskyDao.getWhiskyById(id)

    suspend fun getWhiskyWithTastings(id: String): WhiskyWithTastings? = whiskyDao.getWhiskyWithTastings(id)

    suspend fun insertWhisky(whisky: Whisky) = whiskyDao.insert(whisky)

    suspend fun updateWhisky(whisky: Whisky) = whiskyDao.update(whisky)

    suspend fun deleteWhisky(whisky: Whisky) = whiskyDao.delete(whisky)

    suspend fun deleteWhiskyById(id: String) = whiskyDao.deleteById(id)

    // --- Tasting operations ---
    val allTastings: Flow<List<TastingEntry>> = tastingDao.getAllTastings()

    fun getTastingsByWhiskyId(whiskyId: String): Flow<List<TastingEntry>> =
        tastingDao.getTastingsByWhiskyId(whiskyId)

    suspend fun getTastingById(id: String): TastingEntry? = tastingDao.getTastingById(id)

    suspend fun findDuplicateTasting(whiskyId: String, date: String, alias: String): TastingEntry? =
        tastingDao.findByWhiskyDateAlias(whiskyId, date, alias)

    suspend fun insertTasting(tasting: TastingEntry) = tastingDao.insert(tasting)

    suspend fun updateTasting(tasting: TastingEntry) = tastingDao.update(tasting)

    suspend fun deleteTasting(tasting: TastingEntry) = tastingDao.delete(tasting)

    suspend fun deleteTastingById(id: String) = tastingDao.deleteById(id)

    // --- Tasting + Aromas combined save ---
    suspend fun saveTastingWithAromas(tasting: TastingEntry, aromas: List<TastingAroma>) {
        tastingDao.insert(tasting)
        aromaDao.clearAllTastingAromas(tasting.id)
        if (aromas.isNotEmpty()) {
            aromaDao.insertAllTastingAromas(aromas)
        }
    }

    suspend fun updateTastingWithAromas(tasting: TastingEntry, aromas: List<TastingAroma>) {
        tastingDao.update(tasting)
        aromaDao.clearAllTastingAromas(tasting.id)
        if (aromas.isNotEmpty()) {
            aromaDao.insertAllTastingAromas(aromas)
        }
    }

    // --- Aroma operations ---
    val allAromaTags: Flow<List<AromaTag>> = aromaDao.getAllTags()

    val topAromaTags: Flow<List<AromaTagCount>> = aromaDao.getTopAromaTagCounts()

    suspend fun getAromasForTasting(tastingId: String): List<TastingAroma> =
        aromaDao.getAromasForTasting(tastingId)

    // --- Export / Import ---
    suspend fun exportToZip(context: Context): File =
        ExportManager(context, whiskyDao, tastingDao, aromaDao).exportToZip()

    suspend fun importFromZip(context: Context, uri: Uri): ImportResult =
        ImportManager(context, whiskyDao, tastingDao, aromaDao).importFromZip(uri)

    suspend fun seedAromaTagsIfEmpty() {
        if (aromaDao.getTagCount() == 0) {
            aromaDao.insertAllTags(DefaultAromaTags.all())
        }
    }
}
