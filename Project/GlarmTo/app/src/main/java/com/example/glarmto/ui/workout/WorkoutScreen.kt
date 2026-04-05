package com.example.glarmto.ui.workout

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glarmto.GlarmToApplication
import com.example.glarmto.data.util.ExercisePresets
import com.example.glarmto.ui.theme.GlarmToTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as GlarmToApplication
    val viewModel: WorkoutViewModel = viewModel(
        factory = WorkoutViewModelFactory(application.repository)
    )

    val workouts by viewModel.workouts.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val customRoutines by viewModel.customRoutines.collectAsState()
    val defaultRestTime by viewModel.defaultRestTime.collectAsState()

    var exerciseName by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }

    var routineQueue by remember { mutableStateOf(listOf<String>()) }
    var showRoutineDialog by remember { mutableStateOf(false) }

    // Rest Timer State
    var restTimeSeconds by remember { mutableStateOf(0) }
    var initialRestTime by remember { mutableStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning, restTimeSeconds) {
        if (isTimerRunning && restTimeSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            restTimeSeconds -= 1
            if (restTimeSeconds == 0) {
                isTimerRunning = false
            }
        }
    }

    val isWorkoutDateValid = remember(selectedDate) {
        val cal = Calendar.getInstance()
        val todayStart = cal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val todayEnd = todayStart + (24 * 60 * 60 * 1000L) - 1
        val yesterdayStart = todayStart - (24 * 60 * 60 * 1000L)
        selectedDate in yesterdayStart..todayEnd
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
    )

    val focusManager = LocalFocusManager.current

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.setSelectedDate(it)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Rest Timer UI
        AnimatedVisibility(
            visible = isTimerRunning && restTimeSeconds > 0,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Timer, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Resting...", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    
                    val progress = if (initialRestTime > 0) restTimeSeconds.toFloat() / initialRestTime.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth().height(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${restTimeSeconds}s", 
                            fontSize = 24.sp, 
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { 
                                if (restTimeSeconds > 30) restTimeSeconds -= 30 
                                else restTimeSeconds = 1
                            }) {
                                Text("-30s")
                            }
                            TextButton(onClick = { restTimeSeconds += 30; initialRestTime += 30 }) {
                                Text("+30s")
                            }
                            Button(
                                onClick = { restTimeSeconds = 0; isTimerRunning = false },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Skip")
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dateStr = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date(selectedDate))
            Column {
                Text("Workout on $dateStr", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                if (selectedDate < Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }.timeInMillis || selectedDate > System.currentTimeMillis()) {
                    TextButton(
                        onClick = { viewModel.setSelectedDate(System.currentTimeMillis()) },
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Go to Today", fontSize = 14.sp)
                    }
                }
            }
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = "Change Date", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Add Data Form (Only show if date is valid)
        if (isWorkoutDateValid) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Log Exercise", fontWeight = FontWeight.SemiBold)
                if (customRoutines.isNotEmpty()) {
                    TextButton(onClick = { 
                        if (routineQueue.isNotEmpty()) {
                            exerciseName = routineQueue.first()
                            routineQueue = routineQueue.drop(1)
                        } else {
                            showRoutineDialog = true 
                        }
                    }) {
                        Icon(Icons.Filled.ListAlt, contentDescription = "Load")
                        Spacer(Modifier.width(4.dp))
                        Text(if (routineQueue.isNotEmpty()) "Next: ${routineQueue.first()} (${routineQueue.size})" else "Load Routine")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    val filteredExercises = ExercisePresets.allExercises.filter { 
                        it.contains(exerciseName, ignoreCase = true) 
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = exerciseName,
                            onValueChange = { 
                                exerciseName = it
                                expanded = it.isNotBlank() && filteredExercises.isNotEmpty()
                            },
                            label = { Text("Exercise Name (e.g., Bench Press)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f),
                            properties = androidx.compose.ui.window.PopupProperties(focusable = false)
                        ) {
                            filteredExercises.take(5).forEach { selection ->
                                DropdownMenuItem(
                                    text = { Text(selection) },
                                    onClick = {
                                        exerciseName = selection
                                        expanded = false
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = reps,
                            onValueChange = { reps = it },
                            label = { Text("Reps") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                val w = weight.trim().toDoubleOrNull() ?: 0.0
                                val r = reps.trim().toIntOrNull() ?: 0

                                if (exerciseName.isNotBlank() && w > 0 && r > 0) {
                                    viewModel.addWorkout(exerciseName.trim(), w, r)
                                    
                                    // Start Rest Timer (using user's default)
                                    val dr = defaultRestTime
                                    restTimeSeconds = dr
                                    initialRestTime = dr
                                    isTimerRunning = true
                                    
                                    // Keep exercise name but clear weight/reps for next set
                                    weight = ""
                                    reps = ""
                                    focusManager.clearFocus()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Add Set")
                        }

                        // Copy Previous Set
                        FilledTonalButton(
                            onClick = {
                                if (workouts.isNotEmpty()) {
                                    val lastWorkout = workouts.last()
                                    exerciseName = lastWorkout.exerciseName
                                    weight = lastWorkout.weight.toString()
                                    reps = lastWorkout.reps.toString()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Copy Last")
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    "Editing past data is disabled. You can only log workouts for yesterday or today.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp
                )
            }
        }

        Divider()
        Text("Sets Logged:", fontWeight = FontWeight.SemiBold)

        // List of Logged Workouts
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(workouts) { workout ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(workout.exerciseName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${workout.weight} kg x ${workout.reps} reps", color = MaterialTheme.colorScheme.primary)
                        }
                        if (isWorkoutDateValid) {
                            IconButton(onClick = { viewModel.deleteWorkout(workout.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRoutineDialog && customRoutines.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showRoutineDialog = false },
            title = { Text("Select a Routine") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(customRoutines) { routine ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    val exercises = routine.exercises.split("|")
                                    if (exercises.isNotEmpty()) {
                                        exerciseName = exercises.first()
                                        routineQueue = exercises.drop(1)
                                    }
                                    showRoutineDialog = false
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = routine.routineName,
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRoutineDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
