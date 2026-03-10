package com.example.a517lablearnandroid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest


class ListActivity : ComponentActivity() {

    private val viewModel: PokemonViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("Lifecycle", "ListActivity : onCreate")
        enableEdgeToEdge()
        setContent {
            PokemonListScreen(viewModel)
        }
    }
}

@Composable
fun PokemonListScreen(viewModel: PokemonViewModel) {

    val pokemonList by viewModel.pokemonList.collectAsState()

    // ท่าง่าย
    Column(modifier = Modifier.fillMaxSize().background(Color.Red).padding(16.dp)) {
        Column(modifier = Modifier.fillMaxSize().background(Color.Gray).padding(16.dp)) {
            LazyColumn(modifier = Modifier.fillMaxSize().background(Color.White).padding(16.dp)) {
                items(pokemonList) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.entry_number.toString(),
                            modifier = Modifier.width(40.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = item.pokemon_species.name,
                            modifier = Modifier.weight(1f) // ให้ชื่อกินพื้นที่ที่เหลือ ดันรูปไปขวาสุด
                        )

                        val imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${item.entry_number}.png"

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true) // ให้รูปค่อยๆ เฟดขึ้นมาเมื่อโหลดเสร็จ
                                .build(),
                            contentDescription = "Sprite of ${item.pokemon_species.name}",
                            modifier = Modifier
                                .size(64.dp),
                            placeholder = painterResource(id = R.drawable.ic_launcher_foreground), // รูปขณะโหลด
                            error = painterResource(id = R.drawable.ic_launcher_background) // รูปเมื่อโหลดพลาด
                        )
                    }
                }
            }
        }
    }
}

// Check in 10/3/2026
