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
    fun getTodayWorkouts(): Flow<List<WorkoutEntity>> {
        val (start, end) = getDayRange(Calendar.getInstance())
        return getWorkoutsForRange(start, end)
    }

    fun getWorkoutsForDay(dateMillis: Long): Flow<List<WorkoutEntity>> {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val (start, end) = getDayRange(cal)
        return getWorkoutsForRange(start, end)
    }

    fun getWorkoutsForRange(start: Long, end: Long): Flow<List<WorkoutEntity>> {
        val username = sessionManager.getCurrentUser() ?: return emptyFlow()
        return dao.getWorkoutsForDate(start, end, username)
    }

    suspend fun insertWorkout(workout: WorkoutEntity) {
        val username = sessionManager.getCurrentUser() ?: return
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dao.insertWorkout(workout.copy(username = username))
            awardXP(10) // 10 XP per set
        }
    }

    suspend fun deleteWorkout(id: Int) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dao.deleteWorkout(id)
        }
    }

    // Workout Session Streams
    suspend fun insertWorkoutSession(session: com.example.glarmto.data.local.entity.WorkoutSessionEntity): Long {
        val username = sessionManager.getCurrentUser() ?: return 0L
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dao.insertWorkoutSession(session.copy(username = username))
        }
    }

    suspend fun updateWorkoutSession(session: com.example.glarmto.data.local.entity.WorkoutSessionEntity) {
        val username = sessionManager.getCurrentUser() ?: return
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dao.updateWorkoutSession(session.copy(username = username))
        }
    }

    fun getWorkoutSessionsForDay(dateMillis: Long): Flow<List<com.example.glarmto.data.local.entity.WorkoutSessionEntity>> {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val (start, end) = getDayRange(cal)
        val username = sessionManager.getCurrentUser() ?: return emptyFlow()
        return dao.getWorkoutSessionsForDate(start, end, username)
    }

    fun getWorkoutsForSession(sessionId: Int): Flow<List<WorkoutEntity>> {
        return dao.getWorkoutsForSession(sessionId)
    }

    // Nutrition Streams
    fun getTodayNutrition(): Flow<List<NutritionEntity>> {
        val (start, end) = getDayRange(Calendar.getInstance())
        return getNutritionForRange(start, end)
    }

    fun getNutritionForDay(dateMillis: Long): Flow<List<NutritionEntity>> {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        val (start, end) = getDayRange(cal)
        return getNutritionForRange(start, end)
    }

    fun getNutritionForRange(start: Long, end: Long): Flow<List<NutritionEntity>> {
        val username = sessionManager.getCurrentUser() ?: return emptyFlow()
        return dao.getNutritionForDate(start, end, username)
    }

    suspend fun insertNutrition(nutrition: NutritionEntity) {
        val username = sessionManager.getCurrentUser() ?: return
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            dao.insertNutrition(nutrition.copy(username = username))
            awardXP(5) // 5 XP per food item
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

    fun getTotalXPThreshold(level: Int): Long {
        if (level <= 1) return 0L
        return (1000.0 * (Math.pow(1.1, (level - 1).toDouble()) - 1.0)).toLong()
    }

    fun getXPRequiredForNextLevel(currentLevel: Int): Long {
        return (100.0 * Math.pow(1.1, (currentLevel - 1).toDouble())).toLong()
    }

    suspend fun awardXP(amount: Int) {
        val username = sessionManager.getCurrentUser() ?: return
        val user = dao.getUser(username).firstOrNull() ?: return
        
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val currentDailyXP = if (user.lastXPDate < today) 0L else user.dailyXPEarned
        
        // XP Cap: 300 per day
        val xpToAward = if (currentDailyXP + amount > 300) {
            (300 - currentDailyXP).coerceAtLeast(0).toInt()
        } else {
            amount
        }
        
        if (xpToAward <= 0) return

        val newTotalXP = user.xp + xpToAward
        val newDailyXP = currentDailyXP + xpToAward
        
        // Level Formula: L = log1.1(XP/1000 + 1) + 1
        val newLevel = (Math.log(newTotalXP / 1000.0 + 1.0) / Math.log(1.1)).toInt() + 1
        
        dao.updateUser(user.copy(
            xp = newTotalXP, 
            level = newLevel,
            dailyXPEarned = newDailyXP,
            lastXPDate = today
        ))
    }
}
