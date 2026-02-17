package com.example.whiskytastingjournal.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "tasting_aromas",
    primaryKeys = ["tastingId", "aromaId", "senseType"],
    foreignKeys = [
        ForeignKey(
            entity = TastingEntry::class,
            parentColumns = ["id"],
            childColumns = ["tastingId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AromaTag::class,
            parentColumns = ["id"],
            childColumns = ["aromaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tastingId"), Index("aromaId")]
)
data class TastingAroma(
    val tastingId: String,
    val aromaId: String,
    val senseType: String // "NOSE", "PALATE", "FINISH"
)
