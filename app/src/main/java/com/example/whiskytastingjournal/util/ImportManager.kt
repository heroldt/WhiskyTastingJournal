package com.example.whiskytastingjournal.util

import android.content.Context
import android.net.Uri
import com.example.whiskytastingjournal.data.dao.AromaDao
import com.example.whiskytastingjournal.data.dao.TastingDao
import com.example.whiskytastingjournal.data.dao.WhiskyDao
import com.example.whiskytastingjournal.model.AromaTag
import com.example.whiskytastingjournal.model.SenseType
import com.example.whiskytastingjournal.model.TastingAroma
import com.example.whiskytastingjournal.model.TastingEntry
import com.example.whiskytastingjournal.model.Whisky
import com.example.whiskytastingjournal.model.export.ExportData
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.zip.ZipInputStream
import kotlin.math.abs

data class ImportResult(
    val whiskiesImported: Int,
    val tastingsImported: Int,
    val photosImported: Int,
    val errors: List<String>
)

class ImportManager(
    private val context: Context,
    private val whiskyDao: WhiskyDao,
    private val tastingDao: TastingDao,
    private val aromaDao: AromaDao
) {
    private val gson = Gson()

    suspend fun importFromZip(uri: Uri): ImportResult {
        val tempDir = File(context.cacheDir, "import_temp")
        tempDir.deleteRecursively()
        tempDir.mkdirs()
        val photoDir = context.getExternalFilesDir("photos")?.also { it.mkdirs() }

        val errors = mutableListOf<String>()
        var whiskiesImported = 0
        var tastingsImported = 0
        var photosImported = 0

        try {
            // Extract ZIP to temp directory
            context.contentResolver.openInputStream(uri)?.use { input ->
                ZipInputStream(input).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        if (!entry.isDirectory) {
                            val outFile = File(tempDir, entry.name)
                            outFile.parentFile?.mkdirs()
                            FileOutputStream(outFile).use { out -> zis.copyTo(out) }
                        }
                        zis.closeEntry()
                        entry = zis.nextEntry
                    }
                }
            } ?: return ImportResult(0, 0, 0, listOf("Cannot open file"))

            // Parse data.json
            val jsonFile = File(tempDir, "data.json")
            if (!jsonFile.exists()) return ImportResult(0, 0, 0, listOf("data.json not found in ZIP"))
            val exportData = gson.fromJson(jsonFile.readText(), ExportData::class.java)

            // Load existing data for duplicate detection
            val existingWhiskies = whiskyDao.getAllWhiskiesOnce()
            val existingTastings = tastingDao.getAllTastingsOnce()

            // Build tag name→id map; create any new tags from the import in one batch
            val tagNameToId = aromaDao.getAllTagsList()
                .associate { it.name to it.id }
                .toMutableMap()
            val allNeededTagNames = exportData.whiskies
                .flatMap { w -> w.tastings.flatMap { t -> t.noseTags + t.palateTags + t.finishTags } }
                .toSet()
            val missingTags = allNeededTagNames.filter { it !in tagNameToId }
                .map { AromaTag(name = it, category = "Imported") }
            if (missingTags.isNotEmpty()) {
                aromaDao.insertAllTags(missingTags)
                missingTags.forEach { tagNameToId[it.name] = it.id }
            }

            // Import whiskies and tastings
            for (ew in exportData.whiskies) {
                // Duplicate check: same distillery + whiskyName (case-insensitive)
                val existingWhisky = existingWhiskies.find {
                    it.distillery.equals(ew.distillery, ignoreCase = true) &&
                        it.whiskyName.equals(ew.whiskyName, ignoreCase = true)
                }
                val whiskyId: String
                if (existingWhisky == null) {
                    whiskyDao.insert(
                        Whisky(
                            id = ew.id,
                            distillery = ew.distillery,
                            whiskyName = ew.whiskyName,
                            country = ew.country,
                            region = ew.region,
                            batchCode = ew.batchCode,
                            age = ew.age,
                            bottlingYear = ew.bottlingYear,
                            abv = ew.abv,
                            caskType = ew.caskType
                        )
                    )
                    whiskyId = ew.id
                    whiskiesImported++
                } else {
                    whiskyId = existingWhisky.id
                }

                for (et in ew.tastings) {
                    // Duplicate check: same whisky + date + alias
                    val isDuplicate = existingTastings.any {
                        it.whiskyId == whiskyId &&
                            it.date.toString() == et.date &&
                            it.alias == et.alias
                    }
                    if (isDuplicate) continue

                    // Copy bottle photo
                    var absPhotoPath: String? = null
                    et.bottlePhoto?.let { relPath ->
                        val src = File(tempDir, relPath)
                        if (src.exists() && photoDir != null) {
                            val dst = File(photoDir, src.name)
                            src.copyTo(dst, overwrite = true)
                            absPhotoPath = dst.absolutePath
                            photosImported++
                        }
                    }

                    // Insert tasting
                    val autoScore = TastingEntry.computeOverallScoreAuto(
                        et.noseScore, et.palateScore, et.finishScore
                    )
                    val tasting = TastingEntry(
                        id = et.id,
                        whiskyId = whiskyId,
                        date = LocalDate.parse(et.date),
                        alias = et.alias,
                        price = et.price,
                        noseScore = et.noseScore,
                        palateScore = et.palateScore,
                        finishScore = et.finishScore,
                        overallScoreAuto = autoScore,
                        overallScoreUser = et.overallScore.takeIf { abs(it - autoScore) > 0.01f },
                        noseNotes = et.noseNotes,
                        palateNotes = et.palateNotes,
                        finishNotes = et.finishNotes,
                        bottlePhotoPath = absPhotoPath
                    )
                    tastingDao.insert(tasting)

                    // Insert aroma tags
                    val aromas = buildList {
                        et.noseTags.forEach { tagNameToId[it]?.let { id -> add(TastingAroma(tasting.id, id, SenseType.NOSE.name)) } }
                        et.palateTags.forEach { tagNameToId[it]?.let { id -> add(TastingAroma(tasting.id, id, SenseType.PALATE.name)) } }
                        et.finishTags.forEach { tagNameToId[it]?.let { id -> add(TastingAroma(tasting.id, id, SenseType.FINISH.name)) } }
                    }
                    if (aromas.isNotEmpty()) aromaDao.insertAllTastingAromas(aromas)

                    tastingsImported++
                }
            }
        } catch (e: Exception) {
            errors.add(e.message ?: "Unknown error")
        } finally {
            tempDir.deleteRecursively()
        }

        return ImportResult(whiskiesImported, tastingsImported, photosImported, errors)
    }
}
