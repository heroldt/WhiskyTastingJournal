package com.example.whiskytastingjournal.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "whiskies")
data class Whisky(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val distillery: String = "",
    val country: String = "",
    val region: String = "",
    val whiskyName: String = "",
    val batchCode: String = "",
    val age: Int? = null,
    val bottlingYear: Int? = null
)
