package com.example.glarmto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.glarmto.ui.calculator.CalculatorScreen
import com.example.glarmto.ui.dashboard.DashboardScreen
import com.example.glarmto.ui.login.LoginScreen
import com.example.glarmto.ui.nutrition.NutritionScreen
import com.example.glarmto.ui.theme.GlarmToTheme
import com.example.glarmto.ui.workout.WorkoutScreen
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GlarmToTheme {
                val application = applicationContext as GlarmToApplication
                val startDest = if (application.repository.getCurrentUser() != null) {
                    if (application.repository.isProfileSetup()) {
                        Screen.Dashboard.route
                    } else {
                        "onboarding"
                    }
                } else {
                    "login"
                }
                MainScreen(startDest)
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Home)
    object Routines : Screen("routines", "Routines", Icons.Filled.ListAlt)
    object Workout : Screen("workout", "Workout", Icons.Filled.FitnessCenter)
    object Nutrition : Screen("nutrition", "Nutrition", Icons.Filled.Fastfood)
    object Calculator : Screen("calculator", "Profile", Icons.Filled.Person)
}

@Composable
fun MainScreen(startDestination: String) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as GlarmToApplication

    val items = listOf(
        Screen.Dashboard,
        Screen.Routines,
        Screen.Workout,
        Screen.Nutrition,
        Screen.Calculator
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            // Hide bottom bar on login and onboarding screens
            if (currentDestination?.route != "login" && currentDestination?.route != "onboarding") {
                NavigationBar {
                    items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = startDestination, Modifier.padding(innerPadding)) {
            composable("login") { 
                LoginScreen(onLoginSuccess = {
                    val dest = if (application.repository.isProfileSetup()) Screen.Dashboard.route else "onboarding"
                    navController.navigate(dest) {
                        popUpTo("login") { inclusive = true }
                    }
                }) 
            }
            composable("onboarding") {
                com.example.glarmto.ui.onboarding.OnboardingScreen(onComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo("onboarding") { inclusive = true }
                    }
                })
            }
            composable(Screen.Dashboard.route) { 
                DashboardScreen(
                    onLogout = {
                        application.repository.logout()
                        navController.navigate("login") {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    },
                    onNavigateToHistory = {
                        navController.navigate("history")
                    }
                ) 
            }
            composable("history") {
                com.example.glarmto.ui.history.HistoryScreen(onBack = {
                    navController.popBackStack()
                })
            }
            composable(Screen.Routines.route) { com.example.glarmto.ui.routines.RoutinesScreen() }
            composable(Screen.Workout.route) { WorkoutScreen() }
            composable(Screen.Nutrition.route) { NutritionScreen() }
            composable(Screen.Calculator.route) { CalculatorScreen() }
        }
    }
}