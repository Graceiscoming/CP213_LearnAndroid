package com.example.glarmto.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.glarmto.data.local.entity.NutritionEntity
import com.example.glarmto.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GlarmToDao {

    // Workout Queries
    @Query("SELECT * FROM workout_log WHERE dateInMillis >= :startOfDay AND dateInMillis <= :endOfDay ORDER BY id ASC")
    fun getWorkoutsForDate(startOfDay: Long, endOfDay: Long): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWorkout(workout: WorkoutEntity): Long

    @Query("DELETE FROM workout_log WHERE id = :id")
    fun deleteWorkout(id: Int): Int

    // Nutrition Queries
    @Query("SELECT * FROM nutrition_log WHERE dateInMillis >= :startOfDay AND dateInMillis <= :endOfDay ORDER BY id ASC")
    fun getNutritionForDate(startOfDay: Long, endOfDay: Long): Flow<List<NutritionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNutrition(nutrition: NutritionEntity): Long

    @Query("DELETE FROM nutrition_log WHERE id = :id")
    fun deleteNutrition(id: Int): Int
}
