package com.example.glarmto.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glarmto.GlarmToApplication
import com.example.glarmto.data.local.entity.UserEntity
import com.example.glarmto.data.util.HealthCalculator
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as GlarmToApplication
    val repository = application.repository
    val coroutineScope = rememberCoroutineScope()
    
    val username = repository.getCurrentUser() ?: ""

    var age by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var isMale by remember { mutableStateOf(true) }

    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome, $username! 👋", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Let's set up your profile to calculate your calorie needs.", textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        
        Spacer(modifier = Modifier.height(32.dp))

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
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Age") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        if (showError) {
            Text("Please enter valid numbers for all fields", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val a = age.trim().toIntOrNull() ?: 0
                val w = weight.trim().toDoubleOrNull() ?: 0.0
                val h = height.trim().toDoubleOrNull() ?: 0.0

                if (a > 0 && w > 0 && h > 0) {
                    showError = false
                    val tdeeResult = HealthCalculator.calculateTdee(a, w, h, isMale)

                    coroutineScope.launch {
                        val user = UserEntity(
                            username = username,
                            age = a,
                            isMale = isMale,
                            weight = w,
                            height = h,
                            dailyGoal = tdeeResult, // TDEE is Maintenance daily goal
                            profileSetup = true
                        )
                        repository.updateUser(user)
                        onComplete()
                    }
                } else {
                    showError = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Save Profile & Continue", fontSize = 16.sp)
        }
    }
}
