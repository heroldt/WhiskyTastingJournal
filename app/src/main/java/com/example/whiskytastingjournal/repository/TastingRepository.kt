package com.example.whiskytastingjournal.repository

import com.example.whiskytastingjournal.data.dao.TastingDao
import com.example.whiskytastingjournal.data.dao.WhiskyDao
import com.example.whiskytastingjournal.model.TastingEntry
import com.example.whiskytastingjournal.model.Whisky
import com.example.whiskytastingjournal.model.WhiskyWithTastings
import kotlinx.coroutines.flow.Flow

class TastingRepository(
    private val tastingDao: TastingDao,
    private val whiskyDao: WhiskyDao
) {

    // Whisky operations
    val allWhiskiesWithTastings: Flow<List<WhiskyWithTastings>> = whiskyDao.getAllWhiskiesWithTastings()

    suspend fun getWhiskyById(id: String): Whisky? = whiskyDao.getWhiskyById(id)

    suspend fun getWhiskyWithTastings(id: String): WhiskyWithTastings? = whiskyDao.getWhiskyWithTastings(id)

    suspend fun insertWhisky(whisky: Whisky) = whiskyDao.insert(whisky)

    suspend fun updateWhisky(whisky: Whisky) = whiskyDao.update(whisky)

    suspend fun deleteWhisky(whisky: Whisky) = whiskyDao.delete(whisky)

    suspend fun deleteWhiskyById(id: String) = whiskyDao.deleteById(id)

    // Tasting operations
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
}
