package com.example.whiskytastingjournal.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.whiskytastingjournal.data.converter.Converters
import com.example.whiskytastingjournal.data.dao.AromaDao
import com.example.whiskytastingjournal.data.dao.TastingDao
import com.example.whiskytastingjournal.data.dao.WhiskyDao
import com.example.whiskytastingjournal.model.AromaTag
import com.example.whiskytastingjournal.model.TastingAroma
import com.example.whiskytastingjournal.model.TastingEntry
import com.example.whiskytastingjournal.model.Whisky

@Database(
    entities = [Whisky::class, TastingEntry::class, AromaTag::class, TastingAroma::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TastingDatabase : RoomDatabase() {

    abstract fun tastingDao(): TastingDao
    abstract fun whiskyDao(): WhiskyDao
    abstract fun aromaDao(): AromaDao

    companion object {
        @Volatile
        private var INSTANCE: TastingDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tastings ADD COLUMN noseScore REAL NOT NULL DEFAULT 5.0")
                db.execSQL("ALTER TABLE tastings ADD COLUMN palateScore REAL NOT NULL DEFAULT 5.0")
                db.execSQL("ALTER TABLE tastings ADD COLUMN finishScore REAL NOT NULL DEFAULT 5.0")
                db.execSQL("ALTER TABLE tastings ADD COLUMN overallScoreAuto REAL NOT NULL DEFAULT 5.0")
                db.execSQL("ALTER TABLE tastings ADD COLUMN overallScoreUser REAL")
                db.execSQL("ALTER TABLE tastings ADD COLUMN noseNotes TEXT")
                db.execSQL("ALTER TABLE tastings ADD COLUMN palateNotes TEXT")
                db.execSQL("ALTER TABLE tastings ADD COLUMN finishNotes TEXT")

                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS aroma_tags (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS tasting_aromas (
                        tastingId TEXT NOT NULL,
                        aromaId TEXT NOT NULL,
                        senseType TEXT NOT NULL,
                        PRIMARY KEY (tastingId, aromaId, senseType),
                        FOREIGN KEY (tastingId) REFERENCES tastings(id) ON DELETE CASCADE,
                        FOREIGN KEY (aromaId) REFERENCES aroma_tags(id) ON DELETE CASCADE
                    )"""
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasting_aromas_tastingId ON tasting_aromas(tastingId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasting_aromas_aromaId ON tasting_aromas(aromaId)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE whiskies ADD COLUMN age INTEGER")
                db.execSQL("ALTER TABLE whiskies ADD COLUMN bottlingYear INTEGER")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE whiskies ADD COLUMN photoPath TEXT")
                db.execSQL("ALTER TABLE tastings ADD COLUMN bottlePhotoPath TEXT")
            }
        }

        fun getDatabase(context: Context): TastingDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TastingDatabase::class.java,
                    "whisky_tasting_db"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
