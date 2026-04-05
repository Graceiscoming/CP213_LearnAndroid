package com.example.glarmto.ui.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as GlarmToApplication
    val viewModel: NutritionViewModel = viewModel(
        factory = NutritionViewModelFactory(application, application.repository)
    )

    val nutritions by viewModel.nutritionList.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var isEditingGoal by remember { mutableStateOf(false) }
    var tempGoal by remember { mutableStateOf(dailyGoal.toString()) }

    val focusManager = LocalFocusManager.current

    val totalConsumed = nutritions.sumOf { it.calories }
    val progress = if (dailyGoal > 0) (totalConsumed.toFloat() / dailyGoal).coerceIn(0f, 1f) else 0f
    val remaining = (dailyGoal - totalConsumed).coerceAtLeast(0)

    val isNutritionDateValid = remember(selectedDate) {
        val cal = Calendar.getInstance()
        val todayStart = cal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val nextWeekEnd = todayStart + (8 * 24 * 60 * 60 * 1000L) - 1
        selectedDate in todayStart..nextWeekEnd
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val todayUtc = cal.timeInMillis
                val nextWeekUtc = todayUtc + (7 * 24 * 60 * 60 * 1000L)
                return utcTimeMillis <= nextWeekUtc
            }
        }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section with Date and Daily Goal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dateStr = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date(selectedDate))
            Text("Nutrition on $dateStr", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = "Change Date", tint = MaterialTheme.colorScheme.primary)
            }
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (isEditingGoal) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = tempGoal,
                        onValueChange = { tempGoal = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(100.dp),
                        singleLine = true
                    )
                    TextButton(onClick = {
                        val newGoal = tempGoal.toIntOrNull()
                        if (newGoal != null && newGoal > 0) {
                            viewModel.updateDailyGoal(newGoal)
                        }
                        isEditingGoal = false
                    }) {
                        Text("Save")
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Goal: $dailyGoal kcal", fontWeight = FontWeight.Medium)
                    if (isNutritionDateValid) {
                        IconButton(onClick = {
                            tempGoal = dailyGoal.toString()
                            isEditingGoal = true
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Goal", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        // Progress Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Consumed: $totalConsumed kcal  |  Remaining: $remaining kcal", fontWeight = FontWeight.SemiBold)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                )
            }
        }

        // Manual Input Form (Only show if date is valid)
        if (isNutritionDateValid) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = foodName,
                        onValueChange = { foodName = it },
                        label = { Text("Food Name (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = calories,
                            onValueChange = { calories = it },
                            label = { Text("Calories (kcal)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                val cal = calories.trim().toIntOrNull()
                                if (cal != null && cal > 0) {
                                    val name = if (foodName.isNotBlank()) foodName.trim() else "Quick Add"
                                    viewModel.addNutrition(foodName = name, calories = cal)
                                    foodName = ""
                                    calories = ""
                                    focusManager.clearFocus()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Add")
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
                    "Editing past data is disabled. You can only plan your meals for today or up to 7 days in the future.",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 14.sp
                )
            }
        }

        Divider()

        // List of eaten foods today
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(nutritions) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.foodName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${item.calories} kcal", color = MaterialTheme.colorScheme.primary)
                        }
                        if (isNutritionDateValid) {
                            IconButton(onClick = { viewModel.deleteNutrition(item.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
