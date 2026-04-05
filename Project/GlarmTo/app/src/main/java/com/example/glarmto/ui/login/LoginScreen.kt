package com.example.glarmto.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glarmto.GlarmToApplication

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val application = context.applicationContext as GlarmToApplication
    val repository = application.repository

    var username by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "GlarmTo",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Simple Log for Big Gains",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { 
                username = it
                isError = false
            },
            label = { Text("Username") },
            isError = isError,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (isError) {
            Text(
                text = "Username cannot be empty",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val user = username.trim()
                if (user.isNotEmpty()) {
                    repository.login(user)
                    onLoginSuccess()
                } else {
                    isError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Login / Register", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}
