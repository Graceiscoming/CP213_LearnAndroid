package com.example.glarmto.data.repository

import com.example.glarmto.data.local.dao.GlarmToDao
import com.example.glarmto.data.local.entity.NutritionEntity
import com.example.glarmto.data.local.entity.RoutineEntity
import com.example.glarmto.data.local.entity.WorkoutEntity
import com.example.glarmto.data.preferences.SessionManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class GlarmToRepository(private val dao: GlarmToDao, private val sessionManager: SessionManager) {

    // Helper to get start and end of a specific day in millis
    fun getDayRange(calendar: Calendar): Pair<Long, Long> {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return Pair(start, end)
    }

    // Workout Streams
    fun getWorkoutsForRange(start: Long, end: Long): Flow<List<WorkoutEntity>> {
        val username = sessionManager.getCurrentUser() ?: return emptyFlow()
        return dao.getWorkoutsForDate(start, end, username)
    }

    fun getTodayWorkouts(): Flow<List<WorkoutEntity>> {
        val (start, end) = getDayRange(Calendar.getInstance())
        return getWorkoutsForRange(start, end)
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
    fun getNutritionForRange(start: Long, end: Long): Flow<List<NutritionEntity>> {
        val username = sessionManager.getCurrentUser() ?: return emptyFlow()
        return dao.getNutritionForDate(start, end, username)
    }

    fun getTodayNutrition(): Flow<List<NutritionEntity>> {
        val (start, end) = getDayRange(Calendar.getInstance())
        return getNutritionForRange(start, end)
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

    // Routine Streams
    fun getRoutines(): Flow<List<RoutineEntity>> {
        val username = sessionManager.getCurrentUser() ?: return emptyFlow()
        return dao.getRoutines(username)
    }

    suspend fun insertRoutine(routine: RoutineEntity) {
        val username = sessionManager.getCurrentUser() ?: return
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dao.insertRoutine(routine.copy(username = username))
        }
    }

    suspend fun deleteRoutine(id: Int) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dao.deleteRoutine(id)
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
