package com.example.glarmto.data.util

import kotlin.random.Random

data class GeneratedWorkout(
    val title: String,
    val totalTimeMins: Int,
    val exercises: List<GeneratedExercise>
)

data class GeneratedExercise(
    val name: String,
    val targetSets: Int,
    val targetReps: String // e.g., "10-12"
)

object WorkoutGenerator {
    /**
     * Offline rule-based AI function to dynamically generate a workout.
     */
    fun generateWorkout(
        availableTimeMins: Int,
        equipmentConstraints: List<Equipment>,
        focusMuscles: List<MuscleGroup>
    ): GeneratedWorkout {
        // 1. Filter the library
        var pool = ExerciseLibrary.exercises
        
        if (equipmentConstraints.isNotEmpty() && !equipmentConstraints.contains(Equipment.None)) {
            // Need to match AT LEAST ONE of the available equipments (or Bodyweight which is always fine)
            pool = pool.filter { 
                equipmentConstraints.contains(it.equipment) || it.equipment == Equipment.Bodyweight 
            }
        }
        
        if (focusMuscles.isNotEmpty()) {
            pool = pool.filter { focusMuscles.contains(it.primaryMuscle) }
        }
        
        // 2. Determine number of exercises based on time
        // Rule of thumb: ~6 mins per exercise (3 sets * ~1 min work + 3 sets * ~1 min rest)
        val targetNumExercises = (availableTimeMins / 6).coerceIn(2, 8) // At least 2, at most 8
        
        // 3. Select exercises
        val selectedExercises = mutableListOf<ExerciseDef>()
        
        // If we have focus muscles, try to pick at least one exercise per focus muscle if possible
        val poolGroups = pool.groupBy { it.primaryMuscle }
        if (focusMuscles.isNotEmpty()) {
            for (muscle in focusMuscles) {
                val exerciseForMuscle = poolGroups[muscle]?.randomOrNull()
                if (exerciseForMuscle != null) {
                    selectedExercises.add(exerciseForMuscle)
                }
            }
        }
        
        // Fill the rest randomly from the pool avoiding duplicates
        val remainingPool = pool.filter { !selectedExercises.contains(it) }.shuffled()
        val needed = targetNumExercises - selectedExercises.size
        if (needed > 0) {
            selectedExercises.addAll(remainingPool.take(needed))
        }

        // Shuffle the final list for variety
        selectedExercises.shuffle(Random(System.currentTimeMillis()))
        
        // 4. Determine volume (sets and reps)
        // Adjust sets based on exactly how much time we have
        val totalSetsCapacity = availableTimeMins / 2 // ~2 mins per set total
        var setsPerExercise = if (selectedExercises.isNotEmpty()) totalSetsCapacity / selectedExercises.size else 3
        setsPerExercise = setsPerExercise.coerceIn(2, 5) // At least 2, at most 5

        val finalExercises = selectedExercises.map { def ->
            val reps = when (def.equipment) {
                Equipment.Bodyweight -> "15-20"
                Equipment.Barbell -> "5-8"
                Equipment.Machine -> "12-15"
                else -> "8-12"
            }
            GeneratedExercise(
                name = def.name,
                targetSets = setsPerExercise,
                targetReps = reps
            )
        }

        val muscleString = if (focusMuscles.isNotEmpty()) focusMuscles.joinToString(" & ") { it.name } else "Full Body"
        
        return GeneratedWorkout(
            title = "AI $muscleString Blast",
            totalTimeMins = availableTimeMins,
            exercises = finalExercises
        )
    }
}
