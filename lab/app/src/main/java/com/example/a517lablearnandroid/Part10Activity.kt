package com.example.a517lablearnandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a517lablearnandroid.ui.theme._517LabLearnAndroidTheme

class Part10Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _517LabLearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GlanceConceptScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun GlanceConceptScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Mission 10: App Widget x Jetpack Glance",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            ConceptCard(
                title = "1. App Widget คืออะไร?",
                description = "คือส่วนขยายของแอปที่แสดงผลบนหน้า Home Screen ของ Android ช่วยให้ผู้ใช้เข้าถึงข้อมูลสำคัญหรือฟังก์ชันพื้นฐานได้โดยไม่ต้องเปิดแอปเต็มๆ"
            )
        }

        item {
            ConceptCard(
                title = "2. Jetpack Glance คืออะไร?",
                description = "เป็นเฟรมเวิร์กใหม่จาก Google ที่ช่วยให้เราเขียนโค้ด App Widget ด้วยภาษาแบบ Declarative คล้าย Jetpack Compose มากๆ ทำให้พัฒนาได้เร็วและปลอดภัยขึ้น"
            )
        }

        item {
            ConceptCard(
                title = "3. ทำไมต้องใช้ Glance?",
                description = "ปกติการเขียน Widget ต้องใช้ RemoteViews ซึ่งซับซ้อนและจำกัดมาก แต่ Glance จะแปลงโค้ด Compose-like ของเราให้เป็น RemoteViews ให้อัตโนมัติ"
            )
        }

        item {
            ConceptCard(
                title = "4. ข้อควรระวัง!",
                description = "Glance ไม่ใช่ Compose ตัวเต็ม! เราไม่สามารถใช้คอมโพเนนต์หรือไลบรารีทุกอย่างของ Compose บน Widget ได้ ต้องใช้คอมโพเนนต์เฉพาะของ Glance เท่านั้น เช่น androidx.glance.layout.Column เป็นต้น"
            )
        }

        item {
            InfoCard()
        }
    }
}

@Composable
fun ConceptCard(title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 14.sp, lineHeight = 20.sp)
        }
    }
}

@Composable
fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "ลองไปที่หน้า Home แล้วเพิ่ม Widget '517Lab' เพื่อดูผลลัพธ์!",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}


