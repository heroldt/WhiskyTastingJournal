package com.example.whiskytastingjournal

import android.app.Application
import com.example.whiskytastingjournal.data.database.TastingDatabase
import com.example.whiskytastingjournal.repository.TastingRepository

class WhiskyApp : Application() {

    val database by lazy { TastingDatabase.getDatabase(this) }
    val repository by lazy { TastingRepository(database.tastingDao(), database.whiskyDao()) }
}
