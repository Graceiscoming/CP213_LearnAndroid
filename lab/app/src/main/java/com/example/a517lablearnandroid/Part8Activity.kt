package com.example.a517lablearnandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a517lablearnandroid.ui.theme._517LabLearnAndroidTheme

class Part8Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _517LabLearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ResponsiveProfileScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun ResponsiveProfileScreen(modifier: Modifier = Modifier) {
    //BoxWithConstraints: สเกลตามพื้นที่ที่ได้รับมาจริง (Breakpoint)
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // เงื่อนไขแบ่ง Layout: ถ้ากว้างน้อยกว่า 600.dp (มือถือแนวตั้ง)
        if (maxWidth < 600.dp) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ProfileImage(size = 180.dp)
                Spacer(modifier = Modifier.height(24.dp))
                ProfileInfo(alignment = Alignment.CenterHorizontally)
            }
        } else {
            // ถ้ากว้างมากกว่าหรือเท่ากับ 600.dp (แนวนอน หรือ Tablet)
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                ProfileImage(size = 220.dp)
                Spacer(modifier = Modifier.width(48.dp))
                ProfileInfo(alignment = Alignment.Start, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ProfileImage(size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.Gray) // แทนที่รูปโปรไฟล์
    )
}

@Composable
fun ProfileInfo(alignment: Alignment.Horizontal, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = alignment
    ) {
        Text(
            text = "Antigravity AI",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "@antigravity_jetpack",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Expert in Responsive Design and Jetpack Compose Animations. Love building fluid and adaptive Android applications.",
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun PreviewMobile() {
    _517LabLearnAndroidTheme {
        ResponsiveProfileScreen()
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 480)
@Composable
fun PreviewTablet() {
    _517LabLearnAndroidTheme {
        ResponsiveProfileScreen()
    }
}