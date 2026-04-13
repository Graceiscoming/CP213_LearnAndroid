package com.example.glarmto.ui.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glarmto.data.util.Equipment
import com.example.glarmto.data.util.GeneratedWorkout
import com.example.glarmto.data.util.MuscleGroup
import com.example.glarmto.data.util.WorkoutGenerator
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiWorkoutGeneratorSheet(
    onDismissRequest: () -> Unit,
    onWorkoutGenerated: (GeneratedWorkout) -> Unit
) {
    var timeMins by remember { mutableStateOf(45f) }
    
    val selectedEquipment = remember { mutableStateListOf<Equipment>() }
    val selectedMuscles = remember { mutableStateListOf<MuscleGroup>() }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.fillMaxHeight(0.85f)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "AI Workout Generator 🎯",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text("Tell us what you have, and our offline AI will build the perfect routine for you.")

            Divider()

            // 1. Time
            Text("Available Time: ${timeMins.roundToInt()} minutes", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Slider(
                value = timeMins,
                onValueChange = { timeMins = it },
                valueRange = 10f..120f,
                steps = 11 // 10, 20, 30...
            )

            // 2. Equipment
            Text("Available Equipment", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Equipment.values()) { eq ->
                    FilterChip(
                        selected = selectedEquipment.contains(eq),
                        onClick = {
                            if (selectedEquipment.contains(eq)) selectedEquipment.remove(eq)
                            else selectedEquipment.add(eq)
                        },
                        label = { Text(eq.name) }
                    )
                }
            }

            // 3. Muscle Focus
            Text("Target Muscles", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(MuscleGroup.values()) { mc ->
                    FilterChip(
                        selected = selectedMuscles.contains(mc),
                        onClick = {
                            if (selectedMuscles.contains(mc)) selectedMuscles.remove(mc)
                            else selectedMuscles.add(mc)
                        },
                        label = { Text(mc.name) }
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    val workout = WorkoutGenerator.generateWorkout(
                        availableTimeMins = timeMins.roundToInt(),
                        equipmentConstraints = selectedEquipment,
                        focusMuscles = selectedMuscles
                    )
                    onWorkoutGenerated(workout)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("GENERATE WORKOUT", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
