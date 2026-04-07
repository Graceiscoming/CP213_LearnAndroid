package com.example.a517lablearnandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a517lablearnandroid.ui.theme._517LabLearnAndroidTheme

@OptIn(ExperimentalMaterial3Api::class)
class Part9Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _517LabLearnAndroidTheme {
                CollapsingScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingScreen(onBack: () -> Unit) {
    // กำหนด Scroll Behavior สำหรับการยุบส่วนหัว (Collapsing)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection), // เชื่อมต่อการ Scroll เข้ากับ AppBar
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Collapsing Toolbar",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior // ใส่พฤติกรรมการ Collapsing
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                CollapsingConceptCard()
            }
            
            // สร้างรายการตัวอย่างเพื่อทดสอบการ Scroll
            items((1..50).toList()) { index ->
                ListItem(
                    headlineContent = { Text("Item #$index") },
                    supportingContent = { Text("Scroll down to see the toolbar collapse") }
                )
            }
        }
    }
}

@Composable
fun CollapsingConceptCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Concept: Collapsing Toolbar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "ใน Jetpack Compose (Material 3) การทำ Collapsing ไม่เหมือนกับ XML ที่ใช้ CoordinatorLayout แต่ใช้วิธีการดังนี้:",
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 15.sp
            )
            Text(
                text = "1. exitUntilCollapsedScrollBehavior: กำหนดพฤติกรรมให้ Header ยุบจนเหลือแค่แถบมาตรฐาน (Pinned)",
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 14.sp
            )
            Text(
                text = "2. scrollBehavior.nestedScrollConnection: ต้องนำไปใส่ใน 'Modifier.nestedScroll' ของ Container หลักเพื่อให้ส่งค่าการเลื่อนไปยัง AppBar",
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 14.sp
            )
            Text(
                text = "3. LargeTopAppBar: เป็น Composable ที่รองรับการขยายตัว (Expanded) และย่อตัว (Collapsed) ได้อย่างสวยงามตามธรรมชาติ",
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 14.sp
            )
        }
    }
}
