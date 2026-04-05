package com.example.glarmto.data.repository

import com.example.glarmto.data.local.dao.GlarmToDao
import com.example.glarmto.data.local.entity.NutritionEntity
import com.example.glarmto.data.local.entity.WorkoutEntity
import com.example.glarmto.data.preferences.SessionManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class GlarmToRepository(private val dao: GlarmToDao, private val sessionManager: SessionManager) {

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
        val username = sessionManager.getCurrentUser() ?: return emptyFlow()
        val (start, end) = getTodayRange()
        return dao.getWorkoutsForDate(start, end, username)
    }

    suspend fun insertWorkout(workout: WorkoutEntity) {
        val username = sessionManager.getCurrentUser() ?: return
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dao.insertWorkout(workout.copy(username = username))
        }
    }

    suspend fun deleteWorkout(id: Int) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dao.deleteWorkout(id)
        }
    }

    // Nutrition Streams
    fun getTodayNutrition(): Flow<List<NutritionEntity>> {
        val username = sessionManager.getCurrentUser() ?: return emptyFlow()
        val (start, end) = getTodayRange()
        return dao.getNutritionForDate(start, end, username)
    }

    suspend fun insertNutrition(nutrition: NutritionEntity) {
        val username = sessionManager.getCurrentUser() ?: return
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dao.insertNutrition(nutrition.copy(username = username))
        }
    }

    suspend fun deleteNutrition(id: Int) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dao.deleteNutrition(id)
        }
    }
    
    // User functions
    fun login(username: String) {
        sessionManager.loginUser(username)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            val user = dao.getUser(username).firstOrNull()
            if (user == null) {
                dao.insertUser(com.example.glarmto.data.local.entity.UserEntity(username = username))
                sessionManager.setProfileSetup(false)
            } else {
                sessionManager.setProfileSetup(user.profileSetup)
            }
        }
    }
    
    fun getUserFlow(): Flow<com.example.glarmto.data.local.entity.UserEntity?> {
        val username = sessionManager.getCurrentUser() ?: return emptyFlow()
        return dao.getUser(username)
    }

    suspend fun updateUser(user: com.example.glarmto.data.local.entity.UserEntity) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dao.updateUser(user)
            sessionManager.setProfileSetup(user.profileSetup)
        }
    }
    
    fun logout() {
        sessionManager.logoutUser()
    }
    
    fun getCurrentUser() = sessionManager.getCurrentUser()
    fun isProfileSetup() = sessionManager.isProfileSetup()
}
