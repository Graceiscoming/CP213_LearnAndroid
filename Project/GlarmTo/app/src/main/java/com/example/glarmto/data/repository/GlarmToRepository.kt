package com.example.glarmto.data.repository

import com.example.glarmto.data.local.dao.GlarmToDao
import com.example.glarmto.data.local.entity.NutritionEntity
import com.example.glarmto.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class GlarmToRepository(private val dao: GlarmToDao) {

    // Helper to get start and end of today in millis
    private fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val start = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }

    // Workout Streams
    fun getTodayWorkouts(): Flow<List<WorkoutEntity>> {
        val (start, end) = getTodayRange()
        return dao.getWorkoutsForDate(start, end)
    }

    suspend fun insertWorkout(workout: WorkoutEntity) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dao.insertWorkout(workout)
        }
    }

    suspend fun deleteWorkout(id: Int) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dao.deleteWorkout(id)
        }
    }

    // Nutrition Streams
    fun getTodayNutrition(): Flow<List<NutritionEntity>> {
        val (start, end) = getTodayRange()
        return dao.getNutritionForDate(start, end)
    }

    suspend fun insertNutrition(nutrition: NutritionEntity) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dao.insertNutrition(nutrition)
        }
    }

    suspend fun deleteNutrition(id: Int) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dao.deleteNutrition(id)
        }
    }
}
