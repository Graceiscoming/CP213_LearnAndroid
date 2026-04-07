package com.example.a517lablearnandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a517lablearnandroid.ui.theme._517LabLearnAndroidTheme

class Part3Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _517LabLearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DonutChartScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun DonutChartScreen(modifier: Modifier = Modifier) {
    val proportions = listOf(30f, 40f, 30f)
    val colors = listOf(
        Color(0xFF6200EE), // Purple
        Color(0xFF03DAC5), // Teal
        Color(0xFFFF0266)  // Pink
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        DonutChart(
            proportions = proportions,
            colors = colors,
            modifier = Modifier.size(250.dp),
            strokeWidth = 40.dp
        )
        
        Text(
            text = "Total 100%",
            style = MaterialTheme.typography.titleMedium,
            fontSize = 18.sp
        )
    }
}

@Composable
fun DonutChart(
    proportions: List<Float>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 40.dp
) {
    val total = proportions.sum()
    val animateProgress = remember { Animatable(0f) }

    // ยิง Animation เมื่อเปิดหน้าจอ
    LaunchedEffect(Unit) {
        animateProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500)
        )
    }

    Canvas(modifier = modifier) {
        var startAngle = -90f // เริ่มที่ด้านบนสุด (12 นาฬิกา)

        proportions.forEachIndexed { index, proportion ->
            val sweepAngle = (proportion / total) * 360f
            
            drawArc(
                color = colors.getOrElse(index) { Color.Gray },
                startAngle = startAngle,
                sweepAngle = sweepAngle * animateProgress.value, // คูณด้วย Progress เพื่อทำให้มัน "วาดเอง"
                useCenter = false,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Butt // จบเส้นแบบตัดตรง (เปลี่ยนเป็น Round ถ้าอยากได้โค้งๆ)
                )
            )
            
            startAngle += sweepAngle // เลื่อนมุมเริ่มต้นไปจุดจบของส่วนที่แล้ว
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DonutChartPreview() {
    _517LabLearnAndroidTheme {
        DonutChartScreen()
    }
}