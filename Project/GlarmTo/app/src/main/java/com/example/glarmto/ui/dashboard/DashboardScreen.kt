package com.example.glarmto.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalDrink
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
    val user by viewModel.user.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val weeklyVolume by viewModel.weeklyVolume.collectAsState()
    val trainingStreak by viewModel.trainingStreakDays.collectAsState()
    val periodStats by viewModel.periodTrainingStats.collectAsState()
    val statsPeriodDays by viewModel.statsPeriodDays.collectAsState()
    val todayWater by viewModel.todayWaterMl.collectAsState()
    val waterGoal by viewModel.waterGoalMl.collectAsState()

    val level = user?.level ?: 1
    val xp = user?.xp ?: 0
    
    val threshold = application.repository.getTotalXPThreshold(level)
    val required = application.repository.getXPRequiredForNextLevel(level)
    val xpProgress = if (required > 0) (xp - threshold).toFloat() / required.toFloat() else 0f

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
                    
                    // Level & XP Bar
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "LVL $level", 
                                color = Color.White, 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f).height(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(xpProgress)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("${(xp - threshold)} / $required XP", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = { viewModel.shareExportJson() }, modifier = Modifier.weight(1f)) {
                    Text("Export JSON", fontSize = 12.sp)
                }
                OutlinedButton(onClick = { viewModel.shareExportCsv() }, modifier = Modifier.weight(1f)) {
                    Text("Export CSV", fontSize = 12.sp)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Training insights", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Current streak: $trainingStreak day(s) with at least one set", fontWeight = FontWeight.SemiBold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = statsPeriodDays == 7,
                            onClick = { viewModel.setStatsPeriodDays(7) },
                            label = { Text("7 days") }
                        )
                        FilterChip(
                            selected = statsPeriodDays == 30,
                            onClick = { viewModel.setStatsPeriodDays(30) },
                            label = { Text("30 days") }
                        )
                    }
                    Text(
                        "Volume: ${"%.0f".format(periodStats.totalVolume)} kg · Sets: ${periodStats.totalSets}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalDrink, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Water today", fontWeight = FontWeight.Bold)
                            Text("$todayWater / $waterGoal ml", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    LinearProgressIndicator(
                        progress = {
                            if (waterGoal > 0) (todayWater.toFloat() / waterGoal).coerceIn(0f, 1f) else 0f
                        },
                        modifier = Modifier
                            .width(120.dp)
                            .height(10.dp)
                    )
                }
            }
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
