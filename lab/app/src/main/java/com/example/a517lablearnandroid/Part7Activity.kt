package com.example.a517lablearnandroid

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityOptionsCompat
import com.example.a517lablearnandroid.ui.theme._517LabLearnAndroidTheme

// --- Main Activity for Part 7 ---
class Part7Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _517LabLearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Part7MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onOpenDetail = { message ->
                            val intent = Intent(this, Part7DetailActivity::class.java).apply {
                                putExtra("EXTRA_MESSAGE", message)
                            }
                            
                            // ใช้งาน ActivityOptions เพื่อกำหนด Animation แบบ Slide Up
                            val options = ActivityOptionsCompat.makeCustomAnimation(
                                this,
                                R.anim.slide_up,    // เข้า: เลื่อนขึ้นจากล่าง
                                R.anim.stay_still   // ออก (หน้าเดิม): อยู่นิ่งๆ
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
fun Part7MainScreen(modifier: Modifier = Modifier, onOpenDetail: (String) -> Unit) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Activity Transition Lab",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { onOpenDetail("Hello from Activity A!") }) {
            Text("Open Detail (Slide Up)")
        }
    }
}

// --- Detail Activity for Part 7 ---
class Part7DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "No Message"
        
        setContent {
            _517LabLearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Part7DetailScreen(
                        modifier = Modifier.padding(innerPadding),
                        message = message,
                        onClose = { finish() }
                    )
                }
            }
        }
    }

    override fun finish() {
        super.finish()
        // สั่งให้ทำ Animation แบบ Slide Down เมื่อปิดหน้าจอ
        overridePendingTransition(
            R.anim.stay_still,      // เข้า (หน้าเดิม): อยู่นิ่งๆ
            R.anim.slide_down_exit  // ออก: เลื่อนลงไปข้างล่าง
        )
    }
}

@Composable
fun Part7DetailScreen(modifier: Modifier = Modifier, message: String, onClose: () -> Unit) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Detail Activity",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Received Data:", color = MaterialTheme.colorScheme.secondary)
        Text(text = message, fontSize = 20.sp, fontWeight = FontWeight.Medium)
        
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onClose) {
            Text("Close (Slide Down)")
        }
    }
}
