package com.example.whiskytastingjournal.model

import android.content.Context
import com.example.whiskytastingjournal.R
import org.json.JSONObject

data class Distillery(
    val name: String,
    val country: String,
    val region: String
) {
    val displayLabel: String
        get() = "$name ($region, $country)"

    companion object {
        fun loadAll(context: Context): List<Distillery> {
            val json = context.resources
                .openRawResource(R.raw.distilleries)
                .bufferedReader()
                .use { it.readText() }
            val root = JSONObject(json)
            val array = root.getJSONArray("distilleries")
            return (0 until array.length()).map { i ->
                val obj = array.getJSONObject(i)
                Distillery(
                    name = obj.getString("name"),
                    country = obj.getString("country"),
                    region = obj.getString("region")
                )
            }.sortedBy { it.name.lowercase() }
        }
    }
}
