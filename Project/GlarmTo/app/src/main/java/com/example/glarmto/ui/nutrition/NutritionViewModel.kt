package com.example.glarmto.ui.nutrition

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.glarmto.data.local.entity.NutritionEntity
import com.example.glarmto.data.repository.GlarmToRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NutritionViewModel(
    application: Application,
    private val repository: GlarmToRepository
) : AndroidViewModel(application) {

    val dailyGoal: StateFlow<Int> = repository.getUserFlow()
        .map { it?.dailyGoal ?: 2500 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2500)

    val todayNutrition: StateFlow<List<NutritionEntity>> = repository.getTodayNutrition()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateDailyGoal(goal: Int) {
        viewModelScope.launch {
            val user = repository.getUserFlow().firstOrNull()
            if (user != null) {
                repository.updateUser(user.copy(dailyGoal = goal))
            }
        }
    }

    fun addNutrition(foodName: String, calories: Int) {
        viewModelScope.launch {
            val nutrition = NutritionEntity(
                foodName = foodName,
                calories = calories,
                dateInMillis = System.currentTimeMillis()
            )
            repository.insertNutrition(nutrition)
        }
    }

    fun deleteNutrition(id: Int) {
        viewModelScope.launch {
            repository.deleteNutrition(id)
        }
    }
}

class NutritionViewModelFactory(
    private val application: Application,
    private val repository: GlarmToRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NutritionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NutritionViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
