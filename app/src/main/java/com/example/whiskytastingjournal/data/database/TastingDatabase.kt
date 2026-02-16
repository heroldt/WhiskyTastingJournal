package com.example.whiskytastingjournal.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.whiskytastingjournal.data.converter.Converters
import com.example.whiskytastingjournal.data.dao.TastingDao
import com.example.whiskytastingjournal.model.TastingEntry

@Database(entities = [TastingEntry::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TastingDatabase : RoomDatabase() {

    abstract fun tastingDao(): TastingDao

    companion object {
        @Volatile
        private var INSTANCE: TastingDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tastings ADD COLUMN country TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE tastings ADD COLUMN region TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): TastingDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TastingDatabase::class.java,
                    "whisky_tasting_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
