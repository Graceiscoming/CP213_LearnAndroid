package com.example.glarmto.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.glarmto.data.local.entity.RoutineEntity
import com.example.glarmto.data.local.entity.UserEntity
import com.example.glarmto.data.local.entity.NutritionEntity
import com.example.glarmto.data.local.entity.WorkoutEntity
import com.example.glarmto.data.repository.GlarmToRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class WorkoutViewModel(private val repository: GlarmToRepository) : ViewModel() {

    private val _isWorkingOut = MutableStateFlow(false)
    val isWorkingOut: StateFlow<Boolean> = _isWorkingOut.asStateFlow()

    private val _workoutStartTime = MutableStateFlow(0L)
    val workoutStartTime: StateFlow<Long> = _workoutStartTime.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _currentSessionId = MutableStateFlow<Int?>(null)
    val currentSessionId: StateFlow<Int?> = _currentSessionId.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                if (_isWorkingOut.value && _workoutStartTime.value > 0) {
                    _elapsedSeconds.value = (System.currentTimeMillis() - _workoutStartTime.value) / 1000
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

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
    val workouts: StateFlow<List<WorkoutEntity>> = combine(_selectedDate, _currentSessionId) { date, sessionId -> 
            Pair(date, sessionId) 
        }
        .flatMapLatest { (date, sessionId) ->
            if (sessionId != null) {
                repository.getWorkoutsForSession(sessionId)
            } else {
                val cal = Calendar.getInstance().apply { timeInMillis = date }
                val (start, end) = repository.getDayRange(cal)
                repository.getWorkoutsForRange(start, end)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    val sessionsForDate: StateFlow<List<com.example.glarmto.data.local.entity.WorkoutSessionEntity>> = _selectedDate
        .flatMapLatest { date ->
            repository.getWorkoutSessionsForDay(date)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customRoutines: StateFlow<List<RoutineEntity>> = repository.getRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val defaultRestTime: StateFlow<Int> = repository.getUserFlow()
        .map { it?.defaultRestSeconds ?: 60 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 60)

    val userFlow: StateFlow<UserEntity?> = repository.getUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayNutrition: StateFlow<List<NutritionEntity>> = repository.getTodayNutrition()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun startWorkout() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val session = com.example.glarmto.data.local.entity.WorkoutSessionEntity(
                startTimeInMillis = now,
                dateInMillis = _selectedDate.value
            )
            val id = repository.insertWorkoutSession(session).toInt()
            _currentSessionId.value = id
            _workoutStartTime.value = now
            _elapsedSeconds.value = 0L
            _isWorkingOut.value = true
        }
    }

    fun endWorkout(
        name: String = "",
        notes: String = "",
        exhaustion: Int = 0,
        satisfaction: Int = 0
    ) {
        viewModelScope.launch {
            val sId = _currentSessionId.value ?: return@launch
            val now = System.currentTimeMillis()
            val duration = (now - _workoutStartTime.value) / 1000
            
            val finalName = if (name.isBlank()) {
                val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
                "${sdf.format(java.util.Date(_selectedDate.value))} Workout"
            } else name

            val session = com.example.glarmto.data.local.entity.WorkoutSessionEntity(
                sessionId = sId,
                startTimeInMillis = _workoutStartTime.value,
                endTimeInMillis = now,
                durationSeconds = duration,
                dateInMillis = _selectedDate.value,
                sessionName = finalName,
                notes = notes,
                exhaustionLevel = exhaustion,
                satisfactionLevel = satisfaction
            )
            repository.updateWorkoutSession(session)
            
            _isWorkingOut.value = false
            _currentSessionId.value = null
            _workoutStartTime.value = 0L
            _elapsedSeconds.value = 0L
        }
    }

    fun addWorkout(exerciseName: String, weight: Double, reps: Int) {
        viewModelScope.launch {
            val workout = WorkoutEntity(
                exerciseName = exerciseName,
                weight = weight,
                reps = reps,
                dateInMillis = _selectedDate.value,
                sessionId = _currentSessionId.value
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
