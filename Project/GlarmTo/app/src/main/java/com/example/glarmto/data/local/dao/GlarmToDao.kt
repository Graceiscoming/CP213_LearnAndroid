package com.example.glarmto.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.glarmto.data.local.entity.NutritionEntity
import com.example.glarmto.data.local.entity.RoutineEntity
import com.example.glarmto.data.local.entity.UserEntity
import com.example.glarmto.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GlarmToDao {

    // User Queries
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertUser(user: UserEntity): Long

    @androidx.room.Update
    fun updateUser(user: UserEntity): Int

    @Query("SELECT * FROM user_log WHERE username = :username LIMIT 1")
    fun getUser(username: String): Flow<UserEntity?>

    // Workout Queries
    @Query("SELECT * FROM workout_log WHERE dateInMillis >= :startOfDay AND dateInMillis <= :endOfDay AND username = :username ORDER BY id ASC")
    fun getWorkoutsForDate(startOfDay: Long, endOfDay: Long, username: String): Flow<List<WorkoutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWorkout(workout: WorkoutEntity): Long

    @Query("DELETE FROM workout_log WHERE id = :id")
    fun deleteWorkout(id: Int): Int

    // Nutrition Queries
    @Query("SELECT * FROM nutrition_log WHERE dateInMillis >= :startOfDay AND dateInMillis <= :endOfDay AND username = :username ORDER BY id ASC")
    fun getNutritionForDate(startOfDay: Long, endOfDay: Long, username: String): Flow<List<NutritionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNutrition(nutrition: NutritionEntity): Long

    @Query("DELETE FROM nutrition_log WHERE id = :id")
    fun deleteNutrition(id: Int): Int

    // Routine Queries
    @Query("SELECT * FROM routine_log WHERE username = :username ORDER BY id ASC")
    fun getRoutines(username: String): Flow<List<RoutineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRoutine(routine: RoutineEntity): Long

    @Query("DELETE FROM routine_log WHERE id = :id")
    fun deleteRoutine(id: Int): Int
}
