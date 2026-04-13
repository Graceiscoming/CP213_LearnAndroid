package com.example.glarmto.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.glarmto.data.repository.GlarmToRepository
import com.example.glarmto.data.util.ExerciseLibrary
import com.example.glarmto.data.util.MuscleGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

data class MuscleRecovery(
    val muscleGroup: MuscleGroup,
    val recoveryPercentage: Float // 0.0 to 1.0 (1.0 = 100% recovered)
)

class RecoveryViewModel(private val repository: GlarmToRepository) : ViewModel() {
    private val _recoveryStatus = MutableStateFlow<List<MuscleRecovery>>(emptyList())
    val recoveryStatus: StateFlow<List<MuscleRecovery>> = _recoveryStatus

    private val _smartRecommendation = MutableStateFlow<String>("Analyzing your history...")
    val smartRecommendation: StateFlow<String> = _smartRecommendation

    init {
        fetchAndCalculateRecovery()
    }

    fun fetchAndCalculateRecovery() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val now = System.currentTimeMillis()
                val windowMs = 72 * 60 * 60 * 1000L // 72 hours
                val startWindow = now - windowMs

                // 1. Fetch Workouts
                val recentWorkouts = repository.getWorkoutsBetweenRange(startWindow, now)

                // 2. Map muscle fatigue
                // We'll track the "latest" heavy workout for each muscle, or just add up the damage.
                // Simplified: each set deals 15% damage. Recovery is linear over 48 hours.
                val muscleDamageMap = mutableMapOf<MuscleGroup, Float>()
                val muscleLastHitMap = mutableMapOf<MuscleGroup, Long>()

                for (w in recentWorkouts) {
                    val muscle = ExerciseLibrary.getMuscleFor(w.exerciseName) ?: continue
                    
                    // Track Damage
                    val currentDamage = muscleDamageMap.getOrDefault(muscle, 0f)
                    val damageFromSet = 0.15f // 15% per set
                    muscleDamageMap[muscle] = (currentDamage + damageFromSet).coerceAtMost(1.0f) // Max 100% damage

                    // Track latest time hit
                    val lastHit = muscleLastHitMap.getOrDefault(muscle, 0L)
                    if (w.dateInMillis > lastHit) {
                        muscleLastHitMap[muscle] = w.dateInMillis
                    }
                }

                // 3. Calculate current recovery
                val recoveryList = mutableListOf<MuscleRecovery>()
                val fortyEightHoursMs = 48 * 60 * 60 * 1000L

                for (muscle in MuscleGroup.values()) {
                    val damage = muscleDamageMap.getOrDefault(muscle, 0f)
                    val lastHit = muscleLastHitMap.getOrDefault(muscle, 0L)

                    if (damage == 0f || lastHit == 0L) {
                        recoveryList.add(MuscleRecovery(muscle, 1.0f)) // 100% recovered
                    } else {
                        val timeSinceLastHit = max(0L, now - lastHit)
                        val recoveryProgress = timeSinceLastHit.toFloat() / fortyEightHoursMs.toFloat()
                        
                        // Current status = Initial Damage recovered linearly
                        val remainingDamage = max(0f, damage - recoveryProgress)
                        val recoveryPercentage = 1.0f - remainingDamage
                        recoveryList.add(MuscleRecovery(muscle, recoveryPercentage.coerceIn(0f, 1f)))
                    }
                }

                _recoveryStatus.value = recoveryList

                // 4. Smart Recommendation
                val fullyRecovered = recoveryList.filter { it.recoveryPercentage > 0.9f }.map { it.muscleGroup }
                val exhausted = recoveryList.filter { it.recoveryPercentage < 0.5f }.map { it.muscleGroup }

                if (exhausted.isNotEmpty()) {
                    val recStr = if (fullyRecovered.isNotEmpty()) {
                        "Your ${exhausted.joinToString(", ") { it.name }} are exhausted. Focus on ${fullyRecovered.random().name} today!"
                    } else {
                        "You've been working hard! Everything needs a rest. Take a rest day 🧘‍♂️"
                    }
                    _smartRecommendation.value = recStr
                } else {
                    _smartRecommendation.value = "You are fully recovered! Go crush any workout today 💪"
                }
            }
        }
    }
}

class RecoveryViewModelFactory(private val repository: GlarmToRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecoveryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecoveryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
