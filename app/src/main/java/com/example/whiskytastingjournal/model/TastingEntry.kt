package com.example.whiskytastingjournal.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "tastings")
data class TastingEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val distillery: String = "",
    val country: String = "",
    val region: String = "",
    val whiskyName: String = "",
    val date: LocalDate = LocalDate.now(),
    val price: String = "",
    val batchCode: String = "",
    val notes: String = "",
    val sweetness: Float = 0f,
    val smokiness: Float = 0f,
    val fruitiness: Float = 0f,
    val spice: Float = 0f,
    val body: Float = 0f,
    val finish: Float = 0f
) {
    @get:Ignore
    val overallScore: Float
        get() = listOf(sweetness, smokiness, fruitiness, spice, body, finish)
            .average()
            .toFloat()

    @get:Ignore
    val flavorAttributes: Map<String, Float>
        get() = mapOf(
            "Sweetness" to sweetness,
            "Smokiness" to smokiness,
            "Fruitiness" to fruitiness,
            "Spice" to spice,
            "Body" to body,
            "Finish" to finish
        )
}
