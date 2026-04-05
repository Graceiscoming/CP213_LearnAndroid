package com.example.glarmto.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.glarmto.data.local.dao.GlarmToDao
import com.example.glarmto.data.local.entity.NutritionEntity
import com.example.glarmto.data.local.entity.UserEntity
import com.example.glarmto.data.local.entity.WorkoutEntity

@Database(entities = [WorkoutEntity::class, NutritionEntity::class, UserEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun glarmToDao(): GlarmToDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create user_log table
                db.execSQL("CREATE TABLE IF NOT EXISTS `user_log` (`username` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`username`))")
                // Insert default admin user
                db.execSQL("INSERT OR IGNORE INTO `user_log` (`username`, `createdAt`) VALUES ('admin', ${System.currentTimeMillis()})")
                
                // Add username column to workout_log and nutrition_log
                db.execSQL("ALTER TABLE workout_log ADD COLUMN username TEXT NOT NULL DEFAULT 'admin'")
                db.execSQL("ALTER TABLE nutrition_log ADD COLUMN username TEXT NOT NULL DEFAULT 'admin'")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_log ADD COLUMN age INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE user_log ADD COLUMN isMale INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE user_log ADD COLUMN weight REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE user_log ADD COLUMN height REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE user_log ADD COLUMN dailyGoal INTEGER NOT NULL DEFAULT 2500")
                db.execSQL("ALTER TABLE user_log ADD COLUMN profileSetup INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "glarmto_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
