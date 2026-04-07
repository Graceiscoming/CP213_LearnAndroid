package com.example.a517lablearnandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.a517lablearnandroid.ui.theme._517LabLearnAndroidTheme
import kotlin.random.Random

class Part1Animationactivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _517LabLearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LikeButtonScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun LikeButtonScreen(modifier: Modifier = Modifier) {
    var isLiked by remember { mutableStateOf(false) }
    // เริ่มต้นที่ 0.dp เพื่อให้อยู่ตรงกลางตาม Alignment.Center
    var offsetX by remember { mutableStateOf(0.dp) }
    var offsetY by remember { mutableStateOf(0.dp) }

    // แอนิเมชันขยายขนาด
    val scale by animateFloatAsState(
        targetValue = if (isLiked) 1.2f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "ScaleAnimation"
    )

    // แอนิเมชันเปลี่ยนสี
    val buttonColor by animateColorAsState(
        targetValue = if (isLiked) Color(0xFFFFB6C1) else Color.LightGray,
        label = "ColorAnimation"
    )

    // แอนิเมชันการเคลื่อนที่
    val animOffsetX by animateDpAsState(targetValue = offsetX, animationSpec = spring(stiffness = 300f))
    val animOffsetY by animateDpAsState(targetValue = offsetY, animationSpec = spring(stiffness = 300f))

    // ใช้ BoxWithConstraints และจัดให้อยู่ตรงกลางด้วย Alignment.Center
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // ทำให้ปุ่มเริ่มต้นอยู่ที่กลางจอ
    ) {
        val halfWidth = maxWidth.value / 2
        val halfHeight = maxHeight.value / 2
        
        Button(
            onClick = { 
                isLiked = !isLiked
                
                // สุ่มตำแหน่งใหม่โดยอ้างอิงจากจุดศูนย์กลาง (กระจายออกไปซ้ายขวาบนล่าง)
                // ลบระยะเผื่อ 60dp เพื่อไม่ให้ชนขอบจอจนเกินไป
                val rangeX = (halfWidth - 60).toInt().coerceAtLeast(1)
                val rangeY = (halfHeight - 60).toInt().coerceAtLeast(1)
                
                offsetX = Random.nextInt(-rangeX, rangeX).dp
                offsetY = Random.nextInt(-rangeY, rangeY).dp
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor,
                contentColor = if (isLiked) Color.Red else Color.Black
            ),
            modifier = Modifier
                .offset(x = animOffsetX, y = animOffsetY)
                .scale(scale)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = if (isLiked) "Liked" else "Like")
                
                AnimatedVisibility(
                    visible = isLiked,
                    enter = fadeIn() + scaleIn()
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Heart Icon",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LikeButtonPreview() {
    _517LabLearnAndroidTheme {
        LikeButtonScreen()
    }
}