package com.example.glarmto.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.glarmto.data.local.entity.NutritionEntity
import com.example.glarmto.data.local.entity.WorkoutEntity
import com.example.glarmto.data.repository.GlarmToRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class HistoryViewModel(
    application: Application,
    private val repository: GlarmToRepository
) : AndroidViewModel(application) {

    private val _selectedDate = MutableStateFlow(
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    )
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    val workouts: StateFlow<List<WorkoutEntity>> = _selectedDate
        .flatMapLatest { date ->
            val cal = Calendar.getInstance().apply { timeInMillis = date }
            val (start, end) = repository.getDayRange(cal)
            repository.getWorkoutsForRange(start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    val nutrition: StateFlow<List<NutritionEntity>> = _selectedDate
        .flatMapLatest { date ->
            val cal = Calendar.getInstance().apply { timeInMillis = date }
            val (start, end) = repository.getDayRange(cal)
            repository.getNutritionForRange(start, end)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedDate(date: Long) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        _selectedDate.value = cal.timeInMillis
    }
}

class HistoryViewModelFactory(
    private val application: Application,
    private val repository: GlarmToRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
