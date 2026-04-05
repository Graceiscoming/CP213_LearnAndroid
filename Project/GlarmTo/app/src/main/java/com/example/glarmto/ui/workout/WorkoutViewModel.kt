package com.example.glarmto.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.glarmto.data.local.entity.RoutineEntity
import com.example.glarmto.data.local.entity.WorkoutEntity
import com.example.glarmto.data.repository.GlarmToRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class WorkoutViewModel(private val repository: GlarmToRepository) : ViewModel() {

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

    val customRoutines: StateFlow<List<RoutineEntity>> = repository.getRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val defaultRestTime: StateFlow<Int> = repository.getUserFlow()
        .map { it?.defaultRestSeconds ?: 60 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 60)

    fun setSelectedDate(dateMillis: Long) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        _selectedDate.value = cal.timeInMillis
    }

    fun addWorkout(exerciseName: String, weight: Double, reps: Int) {
        viewModelScope.launch {
            val workout = WorkoutEntity(
                exerciseName = exerciseName,
                weight = weight,
                reps = reps,
                dateInMillis = _selectedDate.value // Save to the currently selected date
            )
            repository.insertWorkout(workout)
        }
    }

    fun deleteWorkout(id: Int) {
        viewModelScope.launch {
            repository.deleteWorkout(id)
        }
    }
}

class WorkoutViewModelFactory(private val repository: GlarmToRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
