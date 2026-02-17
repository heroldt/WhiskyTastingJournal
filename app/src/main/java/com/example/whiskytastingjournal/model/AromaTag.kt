package com.example.whiskytastingjournal.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "aroma_tags")
data class AromaTag(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val category: String = ""
)
