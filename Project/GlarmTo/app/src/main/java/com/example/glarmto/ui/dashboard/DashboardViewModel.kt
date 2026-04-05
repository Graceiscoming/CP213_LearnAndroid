package com.example.glarmto.ui.dashboard

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.glarmto.data.local.entity.NutritionEntity
import com.example.glarmto.data.local.entity.UserEntity
import com.example.glarmto.data.local.entity.WorkoutEntity
import com.example.glarmto.data.repository.GlarmToRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardViewModel(
    private val application: Application,
    repository: GlarmToRepository
) : AndroidViewModel(application) {

    val user: StateFlow<UserEntity?> = repository.getUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dailyGoal: StateFlow<Int> = user
        .map { user -> user?.dailyGoal ?: 2500 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2500)

    fun refreshGoal() {
        // Goal is now refreshed automatically via Room flow
    }

    private fun getTodayStartMillis(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val todayWorkouts: StateFlow<List<WorkoutEntity>> = repository.getWorkoutsForDay(getTodayStartMillis())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayNutrition: StateFlow<List<NutritionEntity>> = repository.getNutritionForDay(getTodayStartMillis())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyVolume: StateFlow<List<Pair<String, Double>>> = repository.getUserFlow()
        .flatMapLatest { user ->
            val cal = Calendar.getInstance()
            val endMillis = repository.getDayRange(cal).second
            cal.add(Calendar.DAY_OF_YEAR, -6)
            val startMillis = repository.getDayRange(cal).first
            
            repository.getWorkoutsForRange(startMillis, endMillis).map { workouts ->
                val sdf = SimpleDateFormat("EEE", Locale.getDefault())
                val volumeMap = mutableMapOf<String, Double>()
                
                // Initialize last 7 days with 0
                for (i in 0..6) {
                    val c = Calendar.getInstance()
                    c.add(Calendar.DAY_OF_YEAR, -i)
                    volumeMap[sdf.format(c.time)] = 0.0
                }
                
                // Fill with actual data
                workouts.forEach { w ->
                    val dayName = sdf.format(java.util.Date(w.dateInMillis))
                    volumeMap[dayName] = (volumeMap[dayName] ?: 0.0) + (w.weight * w.reps)
                }
                
                // Return in chronological order (oldest to newest)
                volumeMap.toList().reversed()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class DashboardViewModelFactory(
    private val application: Application,
    private val repository: GlarmToRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
