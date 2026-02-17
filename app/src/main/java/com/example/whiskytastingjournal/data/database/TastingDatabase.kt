package com.example.whiskytastingjournal.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.whiskytastingjournal.data.converter.Converters
import com.example.whiskytastingjournal.data.dao.TastingDao
import com.example.whiskytastingjournal.data.dao.WhiskyDao
import com.example.whiskytastingjournal.model.TastingEntry
import com.example.whiskytastingjournal.model.Whisky

@Database(entities = [Whisky::class, TastingEntry::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TastingDatabase : RoomDatabase() {

    abstract fun tastingDao(): TastingDao
    abstract fun whiskyDao(): WhiskyDao

    companion object {
        @Volatile
        private var INSTANCE: TastingDatabase? = null

        fun getDatabase(context: Context): TastingDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TastingDatabase::class.java,
                    "whisky_tasting_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
