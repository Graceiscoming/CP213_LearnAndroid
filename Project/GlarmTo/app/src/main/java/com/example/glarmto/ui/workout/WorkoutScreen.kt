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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

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
    val smartSuggestion by viewModel.smartSuggestion.collectAsState()
    val customRoutines by viewModel.customRoutines.collectAsState()
    val defaultRestTime by viewModel.defaultRestTime.collectAsState()
    val isWorkingOut by viewModel.isWorkingOut.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val user by viewModel.userFlow.collectAsState()
    val nutrition by viewModel.todayNutrition.collectAsState()
    val sessions by viewModel.sessionsForDate.collectAsState()

    var exerciseName by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var rpe by remember { mutableStateOf("") }

    LaunchedEffect(exerciseName) {
        viewModel.fetchSmartSuggestion(exerciseName)
    }

    var routineQueue by remember { mutableStateOf(listOf<String>()) }
    var showRoutineDialog by remember { mutableStateOf(false) }
    var showAiGeneratorSheet by remember { mutableStateOf(false) }

    // Rest Timer State
    var restTimeSeconds by remember { mutableStateOf(0) }
    var initialRestTime by remember { mutableStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Finish Workout Dialog State
    var showFinishDialog by remember { mutableStateOf(false) }
    var finishSessionName by remember { mutableStateOf("") }
    var finishNotes by remember { mutableStateOf("") }
    var exhaustionLevel by remember { mutableStateOf(3) }
    var satisfactionLevel by remember { mutableStateOf(3) }

    LaunchedEffect(isTimerRunning, restTimeSeconds) {
        if (isTimerRunning && restTimeSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            restTimeSeconds -= 1
            if (restTimeSeconds == 0) {
                isTimerRunning = false
            }
        }
    }

    var isPipMode by remember { mutableStateOf(false) }
    
    DisposableEffect(context, isTimerRunning) {
        val activity = context as? com.example.glarmto.MainActivity
        
        val pipListener = androidx.core.util.Consumer<androidx.core.app.PictureInPictureModeChangedInfo> { info ->
            isPipMode = info.isInPictureInPictureMode
        }
        
        val leaveHintCallback = {
            if (isTimerRunning && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val params = android.app.PictureInPictureParams.Builder()
                    .setAspectRatio(android.util.Rational(3, 2))
                    .build()
                activity?.enterPictureInPictureMode(params)
            }
        }
        
        activity?.addOnPictureInPictureModeChangedListener(pipListener)
        activity?.onUserLeaveHintCallback = leaveHintCallback
        
        onDispose {
            activity?.removeOnPictureInPictureModeChangedListener(pipListener)
            if (activity?.onUserLeaveHintCallback == leaveHintCallback) {
                activity.onUserLeaveHintCallback = null
            }
        }
    }

    if (isPipMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color(0xFF1E1E1E)),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Timer, contentDescription = null, tint = androidx.compose.ui.graphics.Color.LightGray)
                Text(
                    text = String.format("%d:%02d", restTimeSeconds / 60, restTimeSeconds % 60),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }
        return
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
    val haptic = LocalHapticFeedback.current
    var isShowingConfetti by remember { mutableStateOf(false) }

    if (showAiGeneratorSheet) {
        AiWorkoutGeneratorSheet(
            onDismissRequest = { showAiGeneratorSheet = false },
            onWorkoutGenerated = { generatedWorkout ->
                showAiGeneratorSheet = false
                val newQueue = mutableListOf<String>()
                generatedWorkout.exercises.forEach { ex ->
                    for (i in 0 until ex.targetSets) {
                        newQueue.add(ex.name)
                    }
                }
                routineQueue = newQueue
                viewModel.startWorkout()
                if (newQueue.isNotEmpty()) {
                    exerciseName = newQueue.first()
                    routineQueue = routineQueue.drop(1)
                }
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        viewModel.setSelectedDateFromMaterialPicker(it)
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

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (!isWorkingOut) {
                        val totalCal = nutrition.sumOf { it.calories }
                        val goalCal = user?.dailyGoal ?: 2500

                        Text(
                            "Ready to crush it?",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "Daily Output Summary",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text("Target Calories: $goalCal kcal")
                                Text("Eaten: $totalCal kcal")
                                Text(
                                    "Remaining: ${goalCal - totalCal} kcal",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.startWorkout() },
                                modifier = Modifier.weight(1f).height(64.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("START", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { showAiGeneratorSheet = true },
                                modifier = Modifier.weight(1f).height(64.dp)
                            ) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text("AI WORKOUT", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val mm = elapsedSeconds / 60
                                val ss = elapsedSeconds % 60
                                Text(
                                    String.format("⏱ %02d:%02d", mm, ss),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Button(
                                    onClick = {
                                        showFinishDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("FINISH")
                                }
                            }
                        }
                    }

                    if (showFinishDialog) {
                        AlertDialog(
                            onDismissRequest = { showFinishDialog = false },
                            title = { Text("Workout Summary", fontWeight = FontWeight.Bold) },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = finishSessionName,
                                        onValueChange = { finishSessionName = it },
                                        label = { Text("Session Name (e.g., Heavy Leg Day)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = finishNotes,
                                        onValueChange = { finishNotes = it },
                                        label = { Text("Notes (How did it go?)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 3
                                    )

                                    Text("Exhaustion Level", fontWeight = FontWeight.SemiBold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        (1..5).forEach { level ->
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = null,
                                                tint = if (level <= exhaustionLevel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(32.dp)
                                                    .clickable { exhaustionLevel = level }
                                            )
                                        }
                                    }

                                    Text("Satisfaction Level", fontWeight = FontWeight.SemiBold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        (1..5).forEach { level ->
                                            Icon(
                                                imageVector = Icons.Filled.Star,
                                                contentDescription = null,
                                                tint = if (level <= satisfactionLevel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(32.dp)
                                                    .clickable { satisfactionLevel = level }
                                            )
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                Button(onClick = {
                                    viewModel.endWorkout(
                                        name = finishSessionName,
                                        notes = finishNotes,
                                        exhaustion = exhaustionLevel,
                                        satisfaction = satisfactionLevel
                                    )
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showFinishDialog = false
                                    isTimerRunning = false
                                    restTimeSeconds = 0
                                    finishSessionName = ""
                                    finishNotes = ""
                                    exhaustionLevel = 3
                                    satisfactionLevel = 3
                                    isShowingConfetti = true
                                }) {
                                    Text("SAVE & FINISH")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showFinishDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

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
                                    Text(
                                        "Resting...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(Modifier.height(8.dp))

                                val progress =
                                    if (initialRestTime > 0) restTimeSeconds.toFloat() / initialRestTime.toFloat() else 0f
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
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }) {
                                            Text("-30s")
                                        }
                                        TextButton(onClick = {
                                            restTimeSeconds += 30; initialRestTime += 30
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }) {
                                            Text("+30s")
                                        }
                                        Button(
                                            onClick = {
                                                restTimeSeconds = 0; isTimerRunning = false
                                            },
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
                        val dateStr = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(
                            Date(selectedDate)
                        )
                        Column {
                            Text(
                                "Workout on $dateStr",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (selectedDate < Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(
                                    Calendar.SECOND,
                                    0
                                ); set(Calendar.MILLISECOND, 0)
                                }.timeInMillis || selectedDate > System.currentTimeMillis()) {
                                TextButton(
                                    onClick = { viewModel.setSelectedDateFromLocalInstant(System.currentTimeMillis()) },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("Go to Today", fontSize = 14.sp)
                                }
                            }
                        }
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                Icons.Filled.CalendarMonth,
                                contentDescription = "Change Date",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Add Data Form (Only show if date is valid)
                    if (isWorkoutDateValid && isWorkingOut) {
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
                                            expanded =
                                                it.isNotBlank() && filteredExercises.isNotEmpty()
                                        },
                                        label = { Text("Exercise Name (e.g., Bench Press)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.fillMaxWidth(0.9f),
                                        properties = androidx.compose.ui.window.PopupProperties(
                                            focusable = false
                                        )
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

                                if (!smartSuggestion.isNullOrEmpty()) {
                                    Text(
                                        text = smartSuggestion!!,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                                    )
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
                                OutlinedTextField(
                                    value = rpe,
                                    onValueChange = {
                                        rpe = it.filter { ch -> ch.isDigit() }.take(2)
                                    },
                                    label = { Text("RPE (1–10, optional)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                val wInput = weight.trim().toDoubleOrNull() ?: 0.0
                                val rInput = reps.trim().toDoubleOrNull() ?: 0.0
                                if (wInput > 0 && rInput > 0) {
                                    val oneRm =
                                        if (rInput == 1.0) wInput else wInput * (1 + 0.0333 * rInput)
                                    Text(
                                        text = "Estimated 1RM: ${((oneRm * 10.0).roundToInt() / 10.0)} kg",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
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
                                                val rpeVal =
                                                    rpe.trim().toIntOrNull()?.coerceIn(1, 10)
                                                viewModel.addWorkout(
                                                    exerciseName.trim(),
                                                    w,
                                                    r,
                                                    rpeVal
                                                )
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                                // Start Rest Timer (using user's default)
                                                val dr = defaultRestTime
                                                restTimeSeconds = dr
                                                initialRestTime = dr
                                                isTimerRunning = true

                                                // Keep exercise name but clear weight/reps for next set
                                                weight = ""
                                                reps = ""
                                                rpe = ""
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
                                                rpe = lastWorkout.rpe?.toString() ?: ""
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Filled.ContentCopy,
                                            contentDescription = "Copy",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("Copy Last")
                                    }
                                }
                                OutlinedButton(
                                    onClick = { viewModel.copyWorkoutsFromYesterday() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Copy all sets from yesterday")
                                }
                            }
                        }
                    } else if (!isWorkoutDateValid) {
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
                }
            }

            if (isWorkingOut) {
                item {
                    Text("Current Session Sets:", fontWeight = FontWeight.SemiBold)
                }
                items(workouts) { workout ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    workout.exerciseName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                val w = workout.weight
                                val r = workout.reps
                                val rmText = if (w > 0 && r > 0) {
                                    val calcRm = if (r == 1) w else w * (1 + 0.0333 * r)
                                    " (1RM: ${((calcRm * 10.0).roundToInt() / 10.0)} kg)"
                                } else ""
                                val rpeTxt = workout.rpe?.let { pr -> " · RPE $pr" } ?: ""
                                Text(
                                    "${w} kg x ${r} reps$rmText$rpeTxt",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (isWorkoutDateValid) {
                                IconButton(onClick = { viewModel.deleteWorkout(workout.id) }) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                val workoutsBySession = workouts.groupBy { it.sessionId }
                val matchedSessionIds = mutableSetOf<Int?>()

                item {
                    Text("Logged Sessions:", fontWeight = FontWeight.SemiBold)
                }
                // Show Sessions
                items(sessions) { session ->
                    val sessionWorkouts = workoutsBySession[session.sessionId] ?: emptyList()
                    matchedSessionIds.add(session.sessionId)

                    var expanded by remember { mutableStateOf(false) }
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        session.sessionName.ifBlank { "Session ${session.sessionId}" },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    val mm = session.durationSeconds / 60
                                    val ss = session.durationSeconds % 60
                                    Text(
                                        "${sessionWorkouts.size} sets • Duration: $mm min $ss sec",
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 14.sp
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (session.exhaustionLevel > 0) {
                                        Icon(
                                            Icons.Filled.Star,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            session.exhaustionLevel.toString(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.width(4.dp))
                                    }
                                    Icon(
                                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = "Expand"
                                    )
                                }
                            }

                            AnimatedVisibility(visible = expanded) {
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (session.notes.isNotBlank()) {
                                        Text(
                                            "Notes:",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            session.notes,
                                            fontSize = 14.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                        Divider(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.1f
                                            )
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                "Exhaustion",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                            Row {
                                                (1..5).forEach { i ->
                                                    Icon(
                                                        Icons.Filled.Star,
                                                        null,
                                                        modifier = Modifier.size(12.dp),
                                                        tint = if (i <= session.exhaustionLevel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                            }
                                        }
                                        Column {
                                            Text(
                                                "Satisfaction",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                            Row {
                                                (1..5).forEach { i ->
                                                    Icon(
                                                        Icons.Filled.Star,
                                                        null,
                                                        modifier = Modifier.size(12.dp),
                                                        tint = if (i <= session.satisfactionLevel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Divider(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.1f
                                        )
                                    )

                                    sessionWorkouts.forEach { workout ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    workout.exerciseName,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                val rpeTxt =
                                                    workout.rpe?.let { pr -> " · RPE $pr" } ?: ""
                                                Text(
                                                    "${workout.weight} kg x ${workout.reps} reps$rpeTxt",
                                                    fontSize = 14.sp
                                                )
                                            }
                                            if (isWorkoutDateValid) {
                                                IconButton(onClick = {
                                                    viewModel.deleteWorkout(
                                                        workout.id
                                                    )
                                                }) {
                                                    Icon(
                                                        Icons.Filled.Delete,
                                                        contentDescription = "Delete",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                        if (workout != sessionWorkouts.last()) Divider(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.2f
                                            )
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

                // Show Uncategorized (any workouts whose sessionId was NOT in the sessions list)
                val unmatchedIds = workoutsBySession.keys.filter { it !in matchedSessionIds }
                if (unmatchedIds.isNotEmpty()) {
                    item {
                        var expanded by remember { mutableStateOf(false) }
                        val unmatchedWorkouts =
                            unmatchedIds.flatMap { workoutsBySession[it] ?: emptyList() }

                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Uncategorized Sets",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp
                                        )
                                        Text(
                                            "${unmatchedWorkouts.size} sets",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Icon(
                                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                        contentDescription = "Expand"
                                    )
                                }
                                AnimatedVisibility(visible = expanded) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        unmatchedWorkouts.forEach { workout ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(
                                                        workout.exerciseName,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    val rpeTxt =
                                                        workout.rpe?.let { pr -> " · RPE $pr" }
                                                            ?: ""
                                                    Text(
                                                        "${workout.weight} kg x ${workout.reps} reps$rpeTxt",
                                                        fontSize = 14.sp
                                                    )
                                                }
                                                if (isWorkoutDateValid) {
                                                    IconButton(onClick = {
                                                        viewModel.deleteWorkout(
                                                            workout.id
                                                        )
                                                    }) {
                                                        Icon(
                                                            Icons.Filled.Delete,
                                                            contentDescription = "Delete",
                                                            tint = MaterialTheme.colorScheme.error,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            if (workout != unmatchedWorkouts.last()) Divider(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                    alpha = 0.2f
                                                )
                                            )
                                        }
                                        Spacer(Modifier.height(8.dp))
                                    }
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

        if (isShowingConfetti) {
            val party = Party(
                speed = 0f,
                maxSpeed = 30f,
                damping = 0.9f,
                spread = 360,
                colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                position = nl.dionsegijn.konfetti.core.Position.Relative(0.5, 0.3)
            )
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(party),
            )
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                isShowingConfetti = false
            }
        }
    }
}

