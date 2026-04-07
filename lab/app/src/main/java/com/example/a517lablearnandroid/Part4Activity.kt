package com.example.a517lablearnandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a517lablearnandroid.ui.theme._517LabLearnAndroidTheme
import kotlin.math.roundToInt

class Part4Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _517LabLearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AllGesturesScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AllGesturesScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Total Gestures Showcase",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // 1. Tap & Press
        GestureCard("1. Tap, Double Tap, Long Press") {
            var text by remember { mutableStateOf("Interact with me!") }
            var bgColor by remember { mutableStateOf(Color.LightGray) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(bgColor)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { 
                                text = "Tapped!"
                                bgColor = Color(0xFFBBDEFB)
                            },
                            onDoubleTap = { 
                                text = "Double Tapped!" 
                                bgColor = Color(0xFFC8E6C9)
                            },
                            onLongPress = { 
                                text = "Long Pressed!" 
                                bgColor = Color(0xFFFFF9C4)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            }
        }

        // 2. Drag (Free Movement)
        GestureCard("2. Drag (Free Movement)") {
            var offset by remember { mutableStateOf(Offset.Zero) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.Black.copy(alpha = 0.05f))
                    .clip(MaterialTheme.shapes.medium)
            ) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                offset += dragAmount
                            }
                        }
                )
            }
        }

        // 3. Swipe (Horizontal Locked)
        GestureCard("3. Horizontal Swipe") {
            var offsetX by remember { mutableFloatStateOf(0f) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(Color.Black.copy(alpha = 0.03f)),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.roundToInt(), 0) }
                        .size(width = 100.dp, height = 60.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.tertiary)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                change.consume()
                                offsetX = (offsetX + dragAmount).coerceIn(0f, 600f)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Swipe", color = Color.White)
                }
            }
        }

        // 4. Multitouch (Pinch & Rotate)
        GestureCard("4. Pinch to Zoom & Rotate") {
            var scale by remember { mutableFloatStateOf(1f) }
            var rotation by remember { mutableFloatStateOf(0f) }
            var offset by remember { mutableStateOf(Offset.Zero) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(MaterialTheme.shapes.large)
                    .background(Color.Black.copy(alpha = 0.05f))
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, rotate ->
                            scale *= zoom
                            rotation += rotate
                            offset += pan
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer(
                            scaleX = scale.coerceIn(0.5f, 3f),
                            scaleY = scale.coerceIn(0.5f, 3f),
                            rotationZ = rotation,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .background(MaterialTheme.colorScheme.error)
                        .clip(MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Pinch/Rotate", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun GestureCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AllGesturesPreview() {
    _517LabLearnAndroidTheme {
        AllGesturesScreen()
    }
}