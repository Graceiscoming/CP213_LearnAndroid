package com.example.glarmto

import com.example.glarmto.data.local.dao.GlarmToDao
import com.example.glarmto.data.local.entity.NutritionEntity
import com.example.glarmto.data.local.entity.RoutineEntity
import com.example.glarmto.data.local.entity.UserEntity
import com.example.glarmto.data.local.entity.WorkoutEntity
import com.example.glarmto.data.preferences.SessionManager
import com.example.glarmto.data.repository.GlarmToRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar

/**
 * Manual Fakes to avoid Mockito dependency in current project setup.
 */
class FakeGlarmToDao : GlarmToDao {
    var lastUpdatedUser: UserEntity? = null
    var mockUserFlow = MutableStateFlow<UserEntity?>(null)

    override fun getWorkoutsForDate(startOfDay: Long, endOfDay: Long, username: String) = flowOf(emptyList<WorkoutEntity>())
    override fun insertWorkout(workout: WorkoutEntity): Long = 0L
    override fun deleteWorkout(id: Int): Int = 0
    override fun getNutritionForDate(startOfDay: Long, endOfDay: Long, username: String) = flowOf(emptyList<NutritionEntity>())
    override fun insertNutrition(nutrition: NutritionEntity): Long = 0L
    override fun deleteNutrition(id: Int): Int = 0
    override fun getRoutines(username: String) = flowOf(emptyList<RoutineEntity>())
    override fun insertRoutine(routine: RoutineEntity): Long = 0L
    override fun deleteRoutine(id: Int): Int = 0
    
    override fun getUser(username: String): Flow<UserEntity?> = mockUserFlow
    override fun insertUser(user: UserEntity): Long = 0L
    override fun updateUser(user: UserEntity): Int { 
        lastUpdatedUser = user 
        return 1
    }
}

class FakeSessionManager : SessionManager(null) {
    override fun loginUser(username: String) {}
    override fun logoutUser() {}
    override fun getCurrentUser(): String? = "testuser"
    override fun isProfileSetup(): Boolean = true
    override fun setProfileSetup(setup: Boolean) {}
}

class XPLimitAndMidnightTest {

    private lateinit var fakeDao: FakeGlarmToDao
    private lateinit var fakeSession: FakeSessionManager
    private lateinit var repository: GlarmToRepository

    @Before
    fun setup() {
        fakeDao = FakeGlarmToDao()
        fakeSession = FakeSessionManager()
        repository = GlarmToRepository(fakeDao, fakeSession)
    }

    @Test
    fun `test getDayRange creates correct start and end bounds`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.APRIL, 6, 12, 34)
        }
        val (start, end) = repository.getDayRange(cal)
        
        val startCal = Calendar.getInstance().apply { timeInMillis = start }
        assertEquals(2026, startCal.get(Calendar.YEAR))
        assertEquals(6, startCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, startCal.get(Calendar.HOUR_OF_DAY))
        
        val endCal = Calendar.getInstance().apply { timeInMillis = end }
        assertEquals(23, endCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, endCal.get(Calendar.MINUTE))
    }

    @Test
    fun `test XP awards up to 300 limit per day`() = runBlocking {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val user = UserEntity(
            username = "testuser",
            xp = 1000,
            dailyXPEarned = 295,
            lastXPDate = today
        )
        fakeDao.mockUserFlow.value = user
        
        // Award 10 XP, should only get 5 due to 300 limit
        repository.awardXP(10)
        
        assertEquals(1005.toLong(), fakeDao.lastUpdatedUser?.xp)
        assertEquals(300.toLong(), fakeDao.lastUpdatedUser?.dailyXPEarned)
    }

    @Test
    fun `test XP resets on a new day`() = runBlocking {
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
        }.timeInMillis
        
        val user = UserEntity(
            username = "testuser",
            xp = 500,
            dailyXPEarned = 300,
            lastXPDate = yesterday
        )
        fakeDao.mockUserFlow.value = user
        
        // Award 10 XP on a new day
        repository.awardXP(10)
        
        // Daily XP should reset to 0 + 10 = 10
        assertEquals(10.toLong(), fakeDao.lastUpdatedUser?.dailyXPEarned)
        assertEquals(510.toLong(), fakeDao.lastUpdatedUser?.xp)
    }
}
