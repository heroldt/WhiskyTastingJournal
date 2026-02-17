package com.example.whiskytastingjournal.model

import androidx.room.Embedded
import androidx.room.Relation

data class WhiskyWithTastings(
    @Embedded val whisky: Whisky,
    @Relation(
        parentColumn = "id",
        entityColumn = "whiskyId"
    )
    val tastings: List<TastingEntry>
)
