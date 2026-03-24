package com.example.fitjourney.data.local

import android.content.Context
import androidx.room.*
import com.example.fitjourney.data.local.entity.*
import com.example.fitjourney.data.local.dao.*

@Database(
    entities = [
        UserEntity::class, 
        WorkoutEntity::class, 
        DietEntity::class, 
        StepsEntity::class, 
        WaterEntity::class, 
        WeightEntity::class, 
        ProgressPhotoEntity::class, 
        BodyMeasurementEntity::class,
        ChatMessageEntity::class,
        HabitEntity::class,
        WeeklyReportEntity::class
    ],
    version = 15,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class FitJourneyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun dietDao(): DietDao
    abstract fun stepDao(): StepsDao
    abstract fun waterDao(): WaterDao
    abstract fun weightDao(): WeightDao
    abstract fun photoDao(): PhotoDao
    abstract fun bodyMeasurementDao(): BodyMeasurementDao
    abstract fun chatDao(): ChatDao
    abstract fun habitDao(): HabitDao
    abstract fun weeklyReportDao(): WeeklyReportDao

    companion object {
        @Volatile
        private var INSTANCE: FitJourneyDatabase? = null

        val MIGRATION_10_11 = object :
            androidx.room.migration.Migration(10, 11) {
            override fun migrate(
                db: androidx.sqlite.db.SupportSQLiteDatabase
            ) {
                try {
                    db.execSQL(
                        "ALTER TABLE users ADD COLUMN " +
                        "englishAccent TEXT NOT NULL DEFAULT 'en-in'"
                    )
                } catch (e: Exception) { }
            }
        }

        val MIGRATION_11_12 = object :
            androidx.room.migration.Migration(11, 12) {
            override fun migrate(
                db: androidx.sqlite.db.SupportSQLiteDatabase
            ) {
                try {
                    db.execSQL(
                        "ALTER TABLE users ADD COLUMN " +
                        "englishAccent TEXT NOT NULL DEFAULT 'en-in'"
                    )
                } catch (e: Exception) { }
                try {
                    db.execSQL(
                        "ALTER TABLE habits ADD COLUMN " +
                        "freezesUsedThisWeek INTEGER NOT NULL DEFAULT 0"
                    )
                } catch (e: Exception) { }
                try {
                    db.execSQL(
                        "ALTER TABLE habits ADD COLUMN " +
                        "lastFreezeResetDate TEXT NOT NULL DEFAULT ''"
                    )
                } catch (e: Exception) { }
                try {
                    db.execSQL(
                        "ALTER TABLE users ADD COLUMN " +
                        "speakingLanguage TEXT NOT NULL DEFAULT 'en'"
                    )
                } catch (e: Exception) { }
            }
        }

        val MIGRATION_12_13 = object :
            androidx.room.migration.Migration(12, 13) {
            override fun migrate(
                db: androidx.sqlite.db.SupportSQLiteDatabase
            ) {
                try {
                    db.execSQL(
                        "ALTER TABLE habits ADD COLUMN " +
                        "isMastered INTEGER NOT NULL DEFAULT 0"
                    )
                } catch (e: Exception) { }
            }
        }

        val MIGRATION_9_10 = object :
            androidx.room.migration.Migration(9, 10) {
            override fun migrate(
                db: androidx.sqlite.db.SupportSQLiteDatabase
            ) {
                try {
                    db.execSQL(
                        "ALTER TABLE users ADD COLUMN " +
                        "englishAccent TEXT NOT NULL DEFAULT 'en-in'"
                    )
                } catch (e: Exception) { }
                try {
                    db.execSQL(
                        "ALTER TABLE habits ADD COLUMN " +
                        "freezesUsedThisWeek INTEGER NOT NULL DEFAULT 0"
                    )
                } catch (e: Exception) { }
                try {
                    db.execSQL(
                        "ALTER TABLE habits ADD COLUMN " +
                        "lastFreezeResetDate TEXT NOT NULL DEFAULT ''"
                    )
                } catch (e: Exception) { }
            }
        }

        fun getDatabase(context: Context): FitJourneyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitJourneyDatabase::class.java,
                    "fit_journey_database"
                )
                .addMigrations(MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
