package com.example.glarmto.ui.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun CalculatorScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("BMR / TDEE", "1RM Calculator")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(text = title) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> BmrTdeeCalculator()
                1 -> OneRepMaxCalculator()
            }
        }
    }
}

@Composable
fun BmrTdeeCalculator() {
    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var isMale by remember { mutableStateOf(true) }

    var bmrResult by remember { mutableStateOf(0.0) }
    var tdeeResult by remember { mutableStateOf(0.0) }

    val scrollState = rememberScrollState()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        Text("Calorie Needs Calculator", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FilterChip(
                selected = isMale,
                onClick = { isMale = true },
                label = { Text("Male") }
            )
            FilterChip(
                selected = !isMale,
                onClick = { isMale = false },
                label = { Text("Female") }
            )
        }

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age (years)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                focusManager.clearFocus() // Hide keyboard
                val a = age.trim().toIntOrNull() ?: 0
                val w = weight.trim().toDoubleOrNull() ?: 0.0
                val h = height.trim().toDoubleOrNull() ?: 0.0

                if (a > 0 && w > 0 && h > 0) {
                    // Mifflin-St Jeor Equation
                    bmrResult = if (isMale) {
                        (10 * w) + (6.25 * h) - (5 * a) + 5
                    } else {
                        (10 * w) + (6.25 * h) - (5 * a) - 161
                    }
                    tdeeResult = bmrResult * 1.55 // Assuming moderate activity
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate")
        }

        if (bmrResult > 0) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("BMR: ${bmrResult.roundToInt()} kcal/day", fontWeight = FontWeight.Bold)
                    Text("TDEE (Maintenance): ${tdeeResult.roundToInt()} kcal/day")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Bulking Goal (+300): ${(tdeeResult + 300).roundToInt()} kcal")
                    Text("Cutting Goal (-300): ${(tdeeResult - 300).roundToInt()} kcal")
                }
            }
        }
    }
}

@Composable
fun OneRepMaxCalculator() {
    var weight by remember { mutableStateOf("") }
    var reps by remember { mutableStateOf("") }
    var oneRmResult by remember { mutableStateOf(0.0) }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("1RM Calculator", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Estimate your 1-rep max (Epley formula)", style = MaterialTheme.typography.bodyMedium)

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight Lifted (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = reps,
            onValueChange = { reps = it },
            label = { Text("Reps Performed") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                focusManager.clearFocus() // Hide keyboard
                val w = weight.trim().toDoubleOrNull() ?: 0.0
                val r = reps.trim().toDoubleOrNull() ?: 0.0

                if (w > 0 && r > 0) {
                    oneRmResult = if (r == 1.0) w else w * (1 + 0.0333 * r)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate 1RM")
        }

        if (oneRmResult > 0) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Estimated 1RM: ${((oneRmResult * 10.0).roundToInt() / 10.0)} kg",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
