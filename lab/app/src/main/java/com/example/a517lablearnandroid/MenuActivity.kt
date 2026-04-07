package com.example.a517lablearnandroid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityOptionsCompat
import com.example.a517lablearnandroid.ui.theme._517LabLearnAndroidTheme

class MenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _517LabLearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MenuScreen(
                        modifier = Modifier.padding(innerPadding),
                        onNavigate = { activityClass, enterAnim, exitAnim ->
                            val intent = Intent(this, activityClass)
                            val options = ActivityOptionsCompat.makeCustomAnimation(
                                this, enterAnim, exitAnim
                            )
                            startActivity(intent, options.toBundle())
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MenuScreen(
    modifier: Modifier = Modifier,
    onNavigate: (Class<*>, Int, Int) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Jetpack Compose Lab Menu",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Part 1: Animation Lab - Fade
        MenuButton(
            title = "Animation Lab",
            transitionType = "[Fade Transition]",
            onClick = { onNavigate(Part1Animationactivity::class.java, R.anim.fade_in, R.anim.fade_out) }
        )

        // Part 2: Contact List - Slide Right
        MenuButton(
            title = "Contact List (Sticky Header)",
            transitionType = "[Slide In Right]",
            onClick = { onNavigate(Part2Activity::class.java, R.anim.slide_in_right, R.anim.slide_out_left) }
        )

        // Part 3: Donut Chart - Slide Left
        MenuButton(
            title = "Donut Chart (Canvas)",
            transitionType = "[Slide In Left]",
            onClick = { onNavigate(Part3Activity::class.java, R.anim.slide_in_left, R.anim.slide_out_right) }
        )

        // Part 4: Gestures Showcase - Zoom
        MenuButton(
            title = "Gestures Showcase (PointerInput)",
            transitionType = "[Zoom In Transition]",
            onClick = { onNavigate(Part4Activity::class.java, R.anim.zoom_in, R.anim.zoom_out) }
        )

        // Part 5: LaunchedEffect Lab - Slide Up
        MenuButton(
            title = "LaunchedEffect (Snackbar)",
            transitionType = "[Slide Up Transition]",
            onClick = { onNavigate(Part5Activity::class.java, R.anim.slide_up, R.anim.stay_still) }
        )

        // Part 6: WebView Lab - Fade
        MenuButton(
            title = "WebView Lab (AndroidView)",
            transitionType = "[Fade Transition]",
            onClick = { onNavigate(Part6Avtivity::class.java, R.anim.fade_in, R.anim.fade_out) }
        )

        // Part 7: Activity Transition Lab - Slide Up
        MenuButton(
            title = "Activity Transition Lab",
            transitionType = "[Slide Up Transition]",
            onClick = { onNavigate(Part7Activity::class.java, R.anim.slide_up, R.anim.stay_still) }
        )

        // Part 8: Responsive Lab - BoxWithConstraints
        MenuButton(
            title = "Responsive Profile Lab",
            transitionType = "[Slide In Right]",
            onClick = { onNavigate(Part8Activity::class.java, R.anim.slide_in_right, R.anim.slide_out_left) }
        )
    }
}

@Composable
fun MenuButton(title: String, transitionType: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 16.sp)
            Text(text = transitionType, fontSize = 12.sp, fontWeight = FontWeight.Light)
        }
    }
}
