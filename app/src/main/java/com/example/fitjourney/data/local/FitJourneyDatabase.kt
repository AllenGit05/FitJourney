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
    version = 10,
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

        fun getDatabase(context: Context): FitJourneyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitJourneyDatabase::class.java,
                    "fit_journey_database"
                )
                .fallbackToDestructiveMigration() // Simple for development
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
