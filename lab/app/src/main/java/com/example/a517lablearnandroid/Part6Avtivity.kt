package com.example.a517lablearnandroid

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import com.example.a517lablearnandroid.ui.theme._517LabLearnAndroidTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class Part6Avtivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _517LabLearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WebViewScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

class WebViewModel : ViewModel() {
    private val _url = MutableStateFlow("https://www.google.com")
    val url = _url.asStateFlow()

    fun updateUrl(newUrl: String) {
        // เพิ่มความสะดวกโดยเติม https:// ถ้าไม่มี
        val fullUrl = if (!newUrl.startsWith("http")) "https://$newUrl" else newUrl
        _url.value = fullUrl
    }
}

@Composable
fun WebViewScreen(
    modifier: Modifier = Modifier,
    viewModel: WebViewModel = remember { WebViewModel() }
) {
    val currentUrl by viewModel.url.collectAsState()
    var inputUrl by remember { mutableStateOf("google.com") }

    Column(modifier = modifier.fillMaxSize()) {
        // ส่วนควบคุม URL
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            TextField(
                value = inputUrl,
                onValueChange = { inputUrl = it },
                label = { Text("Enter URL") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Button(
                onClick = { viewModel.updateUrl(inputUrl) },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Go")
            }
        }

        // ส่วนแสดงผล WebView
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                // สร้าง WebView เดิมๆ ในบล็อก factory (รันครั้งเดียว)
                WebView(context).apply {
                    settings.javaScriptEnabled = true // เปิดใช้งาน JS
                    webViewClient = WebViewClient() // โหลดในแอป ไม่เด้งออกไปข้างนอก
                    loadUrl(currentUrl)
                }
            },
            update = { webView ->
                // บล็อก update จะทำงานเมื่อ State ที่ถูกใช้ในนี้เปลี่ยน (เช่น currentUrl)
                // ไม่ต้องสร้าง WebView ใหม่ แค่สั่งให้ตัวเดิมโหลด URL ใหม่พอ
                if (webView.url != currentUrl) {
                    webView.loadUrl(currentUrl)
                }
            }
        )
    }
}