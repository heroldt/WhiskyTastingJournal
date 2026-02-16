package com.example.whiskytastingjournal.repository

import com.example.whiskytastingjournal.data.dao.TastingDao
import com.example.whiskytastingjournal.model.TastingEntry
import kotlinx.coroutines.flow.Flow

// EXTENSION POINT: Cloud Sync
// To add Firebase or REST API sync:
// 1. Create a RemoteDataSource interface with the same CRUD methods
// 2. Inject it alongside the DAO: class TastingRepository(private val dao: TastingDao, private val remote: RemoteDataSource)
// 3. On insert/update/delete, write to both local and remote
// 4. Add a sync() method that pulls remote changes and merges with local data
// 5. Use WorkManager for periodic background sync

// EXTENSION POINT: Data Export (CSV/JSON)
// Add export methods here that query allTastings and serialize:
//   suspend fun exportToCsv(outputStream: OutputStream) { ... }
//   suspend fun exportToJson(outputStream: OutputStream) { ... }
// Call from ViewModel, use Intent.ACTION_CREATE_DOCUMENT to let user pick save location

// EXTENSION POINT: AI Analysis
// To add automatic flavor extraction from tasting notes:
// 1. Create an AnalysisService that sends entry.notes to an LLM API
// 2. Parse the response to extract flavor keywords and suggested slider values
// 3. Call from ViewModel after save: analysisService.analyze(entry.notes)
// 4. Present suggestions to the user for confirmation before updating sliders

class TastingRepository(private val dao: TastingDao) {

    val allTastings: Flow<List<TastingEntry>> = dao.getAllTastings()

    suspend fun getById(id: String): TastingEntry? = dao.getTastingById(id)

    suspend fun insert(tasting: TastingEntry) = dao.insert(tasting)

    suspend fun update(tasting: TastingEntry) = dao.update(tasting)

    suspend fun delete(tasting: TastingEntry) = dao.delete(tasting)

    suspend fun deleteById(id: String) = dao.deleteById(id)
}
