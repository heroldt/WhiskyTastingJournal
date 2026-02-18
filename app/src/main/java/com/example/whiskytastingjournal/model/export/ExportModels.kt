package com.example.whiskytastingjournal.model.export

import com.google.gson.annotations.SerializedName
import java.time.LocalDate

data class ExportData(
    val version: String = "1.0",
    val exportDate: String = LocalDate.now().toString(),
    val whiskies: List<ExportWhisky>
)

data class ExportWhisky(
    val id: String,
    val distillery: String,
    val whiskyName: String,
    val country: String,
    val region: String,
    val batchCode: String,
    val age: Int?,
    val bottlingYear: Int?,
    val tastings: List<ExportTasting>
)

data class ExportTasting(
    val id: String,
    val date: String,
    val alias: String,
    val price: String,
    val noseScore: Float,
    val palateScore: Float,
    val finishScore: Float,
    val overallScore: Float,
    val noseNotes: String?,
    val palateNotes: String?,
    val finishNotes: String?,
    val noseTags: List<String>,
    val palateTags: List<String>,
    val finishTags: List<String>,
    @SerializedName("bottlePhoto")
    val bottlePhoto: String?   // relative path in ZIP, e.g. "photos/uuid_uuid_bottle.jpg"
)
