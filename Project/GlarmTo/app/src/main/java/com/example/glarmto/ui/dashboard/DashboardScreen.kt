package com.example.glarmto.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glarmto.GlarmToApplication

@Composable
fun DashboardScreen(onLogout: () -> Unit = {}, onNavigateToHistory: () -> Unit = {}) {
    val context = LocalContext.current
    val application = context.applicationContext as GlarmToApplication
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(application, application.repository)
    )

    val workouts by viewModel.todayWorkouts.collectAsState()
    val nutritions by viewModel.todayNutrition.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val weeklyVolume by viewModel.weeklyVolume.collectAsState()

    val totalConsumed = nutritions.sumOf { it.calories }
    val progress = if (dailyGoal > 0) (totalConsumed.toFloat() / dailyGoal).coerceIn(0f, 1f) else 0f
    val remaining = (dailyGoal - totalConsumed).coerceAtLeast(0)

    val totalSets = workouts.size
    val totalVolume = workouts.sumOf { it.weight * it.reps }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    val username = application.repository.getCurrentUser() ?: "Guest"
                    Text("Hello, $username! 💪", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text("Summary of your daily progress", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Filled.History, contentDescription = "History")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Workout Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalFireDepartment, contentDescription = "Workout", tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Workout Summary", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Sets", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$totalSets", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Volume", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$totalVolume kg", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        // Nutrition Summary
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = "Nutrition", tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Nutrition Summary", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider()
                    Text("Consumed: $totalConsumed kcal  |  Remaining: $remaining kcal", fontWeight = FontWeight.SemiBold)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                    )
                    Text("Daily Goal: $dailyGoal kcal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Weekly Volume Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Weekly Volume Progress", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    
                    if (weeklyVolume.isEmpty()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        val maxVolume = weeklyVolume.maxOfOrNull { it.second } ?: 1.0
                        val safeMaxVolume = if (maxVolume == 0.0) 1.0 else maxVolume
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            weeklyVolume.forEach { (day, volume) ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    val barHeight = (volume / safeMaxVolume).toFloat().coerceIn(0.05f, 1f)
                                    Box(
                                        modifier = Modifier
                                            .width(24.dp)
                                            .fillMaxHeight(barHeight)
                                            .background(
                                                color = if (volume == safeMaxVolume && volume > 0) 
                                                    MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                shape = MaterialTheme.shapes.extraSmall
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(day, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
