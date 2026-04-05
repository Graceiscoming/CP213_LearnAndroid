package com.example.glarmto.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glarmto.GlarmToApplication
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as GlarmToApplication
    val viewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModelFactory(application, application.repository)
    )

    val selectedDate by viewModel.selectedDate.collectAsState()
    val workouts by viewModel.workouts.collectAsState()
    val nutrition by viewModel.nutrition.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate
    )

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Select Date")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val dateStr = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date(selectedDate))
            Text(dateStr, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            if (workouts.isEmpty() && nutrition.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No logs for this date", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (workouts.isNotEmpty()) {
                        item {
                            Text("Workouts", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            Spacer(Modifier.height(8.dp))
                        }
                        items(workouts) { workout ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(modifier = Modifier.padding(16.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(workout.exerciseName, fontWeight = FontWeight.Bold)
                                        Text("${workout.weight} kg x ${workout.reps} reps")
                                    }
                                }
                            }
                        }
                    }

                    if (nutrition.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(16.dp))
                            Text("Nutrition", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            Spacer(Modifier.height(8.dp))
                        }
                        items(nutrition) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(modifier = Modifier.padding(16.dp)) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.foodName, fontWeight = FontWeight.Bold)
                                        Text("${item.calories} kcal")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
