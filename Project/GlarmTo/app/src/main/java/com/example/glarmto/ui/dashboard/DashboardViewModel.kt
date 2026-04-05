package com.example.glarmto.ui.dashboard

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.glarmto.data.local.entity.NutritionEntity
import com.example.glarmto.data.local.entity.WorkoutEntity
import com.example.glarmto.data.repository.GlarmToRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    private val application: Application,
    repository: GlarmToRepository
) : AndroidViewModel(application) {

    val dailyGoal: StateFlow<Int> = repository.getUserFlow()
        .map { user -> user?.dailyGoal ?: 2500 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2500)

    fun refreshGoal() {
        // Goal is now refreshed automatically via Room flow
    }

    val todayWorkouts: StateFlow<List<WorkoutEntity>> = repository.getTodayWorkouts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayNutrition: StateFlow<List<NutritionEntity>> = repository.getTodayNutrition()
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
