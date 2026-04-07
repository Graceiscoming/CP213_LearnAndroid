package com.example.a517lablearnandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.a517lablearnandroid.ui.theme._517LabLearnAndroidTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class Part2Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _517LabLearnAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ContactListScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

class ContactViewModel : ViewModel() {
    private val allMockData = listOf(
        "Alice", "Avery", "Ariel", "Bob", "Bella", "Brody", "Charlie", "Cora", "Caleb",
        "David", "Daisy", "Dexter", "Eve", "Elena", "Ezra", "Frank", "Fiona", "Finn",
        "Grace", "Gavin", "Giana", "Heidi", "Hugo", "Hazel", "Ivan", "Iris", "Ian",
        "Jack", "Jade", "Jude", "Karl", "Kira", "Kai", "Liam", "Luna", "Leo",
        "Mona", "Milo", "Maya", "Nina", "Nico", "Nora", "Oscar", "Olive", "Owen",
        "Paul", "Piper", "Pude", "Quincy", "Quinn", "Rose", "Ryan", "Ruby",
        "Steve", "Sia", "Seth", "Trent", "Tessa", "Toby", "Ursula", "Umesh",
        "Victor", "Vera", "Vince", "Wendy", "Wyatt", "Willa", "Xavier", "Xena",
        "Yvonne", "Yusuf", "Zack", "Zoe", "Zion"
    )

    private val _contacts = MutableStateFlow<List<String>>(emptyList())
    val contacts = _contacts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var currentPage = 0
    private val pageSize = 15

    init {
        loadMore()
    }

    fun loadMore() {
        if (_isLoading.value || currentPage * pageSize >= allMockData.size) return

        viewModelScope.launch {
            _isLoading.value = true
            delay(2000) // จำลองเวลาในการโหลด 2 วินาที

            val start = currentPage * pageSize
            val end = (start + pageSize).coerceAtMost(allMockData.size)
            val newData = allMockData.subList(start, end)

            _contacts.value += newData
            currentPage++
            _isLoading.value = false
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactListScreen(
    modifier: Modifier = Modifier,
    viewModel: ContactViewModel = viewModel()
) {
    val contacts by viewModel.contacts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val listState = rememberLazyListState()

    // จับกลุ่มข้อมูลตามตัวอักษรแรก (Sticky Header)
    val grouped = remember(contacts) {
        contacts.groupBy { it.first().uppercase() }
    }

    // ตรวจสอบ Pagination เมื่อ Scroll ถึงท้ายสุด
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 2
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !isLoading) {
            viewModel.loadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize()
    ) {
        grouped.forEach { (initial, contactsUnderInitial) ->
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(contactsUnderInitial) { name ->
                ContactItem(name = name)
            }
        }

        // แสดง Loading Indicator ด้านล่างสุด
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
fun ContactItem(name: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = name, fontSize = 18.sp)
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = Color.LightGray
    )
}

@Preview(showBackground = true)
@Composable
fun ContactListPreview() {
    _517LabLearnAndroidTheme {
        ContactListScreen()
    }
}