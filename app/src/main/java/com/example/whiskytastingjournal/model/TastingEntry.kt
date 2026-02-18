package com.example.whiskytastingjournal.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(
    tableName = "tastings",
    foreignKeys = [
        ForeignKey(
            entity = Whisky::class,
            parentColumns = ["id"],
            childColumns = ["whiskyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("whiskyId")]
)
data class TastingEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val whiskyId: String = "",
    val date: LocalDate = LocalDate.now(),
    val alias: String = "",
    val price: String = "",

    // --- New rating fields (1–10 scale) ---
    val noseScore: Float = 5f,
    val palateScore: Float = 5f,
    val finishScore: Float = 5f,
    val overallScoreAuto: Float = 5f,
    val overallScoreUser: Float? = null,

    // --- Per-sense free-text notes ---
    val noseNotes: String? = null,
    val palateNotes: String? = null,
    val finishNotes: String? = null,

    // --- Bottle photo (absolute path on device) ---
    val bottlePhotoPath: String? = null,

    // --- Legacy columns kept for migration compat (unused in new UI) ---
    val notes: String = "",
    val sweetness: Float = 0f,
    val smokiness: Float = 0f,
    val fruitiness: Float = 0f,
    val spice: Float = 0f,
    val body: Float = 0f,
    val finish: Float = 0f
) {
    @get:Ignore
    val effectiveOverallScore: Float
        get() = overallScoreUser ?: overallScoreAuto

    companion object {
        fun computeOverallScoreAuto(nose: Float, palate: Float, finishVal: Float): Float =
            0.3f * nose + 0.5f * palate + 0.2f * finishVal

        fun roundToHalf(value: Float): Float =
            (Math.round(value * 2.0) / 2.0).toFloat()
    }
}
