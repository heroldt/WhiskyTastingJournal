package com.example.whiskytastingjournal.util

import android.content.Context
import com.example.whiskytastingjournal.data.dao.AromaDao
import com.example.whiskytastingjournal.data.dao.TastingDao
import com.example.whiskytastingjournal.data.dao.WhiskyDao
import com.example.whiskytastingjournal.model.SenseType
import com.example.whiskytastingjournal.model.export.ExportData
import com.example.whiskytastingjournal.model.export.ExportTasting
import com.example.whiskytastingjournal.model.export.ExportWhisky
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ExportManager(
    private val context: Context,
    private val whiskyDao: WhiskyDao,
    private val tastingDao: TastingDao,
    private val aromaDao: AromaDao
) {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun exportToZip(): File {
        // Load everything in batch — no N+1
        val whiskiesWithTastings = whiskyDao.getAllWhiskiesWithTastingsOnce()
        val allAromas = aromaDao.getAllTastingAromas()
        val tagById = aromaDao.getAllTagsList().associateBy { it.id }
        val aromasByTasting = allAromas.groupBy { it.tastingId }

        val exportWhiskies = whiskiesWithTastings.map { wt ->
            val exportTastings = wt.tastings.map { tasting ->
                val aromas = aromasByTasting[tasting.id] ?: emptyList()

                fun tagsForSense(sense: SenseType) = aromas
                    .filter { it.senseType == sense.name }
                    .mapNotNull { tagById[it.aromaId]?.name }

                val photoRelPath = tasting.bottlePhotoPath?.let { abs ->
                    val f = File(abs)
                    if (f.exists()) "photos/${f.name}" else null
                }

                ExportTasting(
                    id = tasting.id,
                    date = tasting.date.toString(),
                    alias = tasting.alias,
                    price = tasting.price,
                    noseScore = tasting.noseScore,
                    palateScore = tasting.palateScore,
                    finishScore = tasting.finishScore,
                    overallScore = tasting.effectiveOverallScore,
                    noseNotes = tasting.noseNotes,
                    palateNotes = tasting.palateNotes,
                    finishNotes = tasting.finishNotes,
                    noseTags = tagsForSense(SenseType.NOSE),
                    palateTags = tagsForSense(SenseType.PALATE),
                    finishTags = tagsForSense(SenseType.FINISH),
                    bottlePhoto = photoRelPath
                )
            }
            ExportWhisky(
                id = wt.whisky.id,
                distillery = wt.whisky.distillery,
                whiskyName = wt.whisky.whiskyName,
                country = wt.whisky.country,
                region = wt.whisky.region,
                batchCode = wt.whisky.batchCode,
                age = wt.whisky.age,
                bottlingYear = wt.whisky.bottlingYear,
                abv = wt.whisky.abv,
                caskType = wt.whisky.caskType,
                tastings = exportTastings
            )
        }

        val json = gson.toJson(ExportData(whiskies = exportWhiskies))

        // Write ZIP to app's backups dir (no permissions needed)
        val backupDir = context.getExternalFilesDir("backups") ?: context.filesDir
        backupDir.mkdirs()
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val zipFile = File(backupDir, "whisky_backup_$timestamp.zip")
        val photoDir = context.getExternalFilesDir("photos")

        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            // data.json
            zos.putNextEntry(ZipEntry("data.json"))
            zos.write(json.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // photos
            exportWhiskies.flatMap { it.tastings }.forEach { tasting ->
                tasting.bottlePhoto?.let { relPath ->
                    val filename = relPath.removePrefix("photos/")
                    val src = File(photoDir, filename)
                    if (src.exists()) {
                        zos.putNextEntry(ZipEntry(relPath))
                        src.inputStream().use { it.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }
        }

        return zipFile
    }
}
