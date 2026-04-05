package com.example.glarmto.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glarmto.GlarmToApplication
import java.util.Calendar
import kotlin.math.roundToInt

@Composable
fun CalculatorScreen() {
    val context = LocalContext.current
    val application = context.applicationContext as GlarmToApplication
    val viewModel: CalculatorViewModel = viewModel(
        factory = CalculatorViewModelFactory(application, application.repository)
    )

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("My Profile", "1RM Calculator")

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
                0 -> ProfileEditor(viewModel)
                1 -> OneRepMaxCalculator()
            }
        }
    }
}

@Composable
fun ProfileEditor(viewModel: CalculatorViewModel) {
    val context = LocalContext.current
    val application = context.applicationContext as com.example.glarmto.GlarmToApplication
    val user by viewModel.currentUser.collectAsState()
    
    // We only populate these if we enter edit mode, else we show current stats
    var isEditing by remember { mutableStateOf(false) }
    
    var editAge by remember { mutableStateOf("") }
    var editWeight by remember { mutableStateOf("") }
    var editHeight by remember { mutableStateOf("") }
    var editIsMale by remember { mutableStateOf(true) }
    var editRestTime by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Person, contentDescription = "Profile", tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("User Profile & TDEE", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        user?.let { u ->
            // XP & Level Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("LVL", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("${u.level}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    
                Column(modifier = Modifier.weight(1f)) {
                    val threshold = application.repository.getTotalXPThreshold(u.level)
                    val required = application.repository.getXPRequiredForNextLevel(u.level)
                    val xpProgress = if (required > 0) (u.xp - threshold).toFloat() / required.toFloat() else 0f
                    
                    val today = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.timeInMillis
                    
                    val dailyEarned = if (u.lastXPDate < today) 0 else u.dailyXPEarned

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.MilitaryTech, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        Text("Fitness Journey", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { xpProgress },
                        modifier = Modifier.fillMaxWidth().height(12.dp),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("${u.xp - threshold} / $required XP to Level ${u.level + 1}", style = MaterialTheme.typography.labelSmall)
                    Text("Today's XP: $dailyEarned / 300", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("${u.xp} Total XP earned", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                }
                }
            }

            if (!isEditing) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Username: ${u.username}", fontWeight = FontWeight.Bold)
                        Text("Age: ${u.age} years")
                        Text("Gender: ${if (u.isMale) "Male" else "Female"}")
                        Text("Weight: ${u.weight} kg")
                        Text("Height: ${u.height} cm")
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(4.dp))
                            Text("Default Rest Time: ${u.defaultRestSeconds}s", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Text("Daily Calorie Target: ${u.dailyGoal} kcal", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        
                        Button(onClick = { 
                            editAge = u.age.toString()
                            editWeight = u.weight.toString()
                            editHeight = u.height.toString()
                            editIsMale = u.isMale
                            editRestTime = u.defaultRestSeconds.toString()
                            isEditing = true 
                        }, modifier = Modifier.padding(top = 8.dp)) {
                            Text("Edit Profile")
                        }
                    }
                }
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Edit Profile", fontWeight = FontWeight.Bold)
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            FilterChip(
                                selected = editIsMale,
                                onClick = { editIsMale = true },
                                label = { Text("Male") }
                            )
                            FilterChip(
                                selected = !editIsMale,
                                onClick = { editIsMale = false },
                                label = { Text("Female") }
                            )
                        }

                        OutlinedTextField(
                            value = editAge,
                            onValueChange = { editAge = it },
                            label = { Text("Age (years)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editWeight,
                            onValueChange = { editWeight = it },
                            label = { Text("Weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editHeight,
                            onValueChange = { editHeight = it },
                            label = { Text("Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editRestTime,
                            onValueChange = { editRestTime = it },
                            label = { Text("Default Rest Time (seconds)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { isEditing = false }, modifier = Modifier.weight(1f)) {
                                Text("Cancel")
                            }
                            Button(onClick = {
                                focusManager.clearFocus()
                                val a = editAge.trim().toIntOrNull() ?: u.age
                                val w = editWeight.trim().toDoubleOrNull() ?: u.weight
                                val h = editHeight.trim().toDoubleOrNull() ?: u.height
                                val r = editRestTime.trim().toIntOrNull() ?: u.defaultRestSeconds
                                
                                viewModel.updateProfile(age = a, weight = w, height = h, isMale = editIsMale, restSeconds = r)
                                isEditing = false
                            }, modifier = Modifier.weight(1f)) {
                                Text("Save")
                            }
                        }
                    }
                }
            }
            
            // Add educational info about TDEE
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                 Column(modifier = Modifier.padding(16.dp)) {
                     Text("About your Calorie Target", fontWeight = FontWeight.Bold)
                     Spacer(modifier = Modifier.height(4.dp))
                     Text("Your target is automatically calculated using the Mifflin-St Jeor formula to determine your Total Daily Energy Expenditure (TDEE) assuming moderate activity. If your goal is to lose weight, aim to eat 300-500 kcal less than this target. To gain muscle, eat 300 kcal more.", style = MaterialTheme.typography.bodySmall)
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

    val focusManager = LocalFocusManager.current

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
