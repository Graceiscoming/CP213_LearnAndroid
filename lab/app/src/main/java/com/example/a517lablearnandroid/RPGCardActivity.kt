package com.example.a517lablearnandroid

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class RPGCardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {

            Column(modifier = Modifier.fillMaxSize()
                .background(color=Color.Gray)
                .padding(32.dp))  {
                //hp
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(color=Color.White)
                ) {
                    Text(
                        text = "HP",
                        modifier = Modifier.align(alignment= Alignment.CenterStart)
                            .fillMaxWidth(fraction = 0.17f)
                            .background(color=Color.Red)
                            .padding(8.dp)


                    )

                }

                //image
                Image(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(300.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(top= 16.dp)
                        .clickable() {
                            startActivity(Intent(this@RPGCardActivity, MainActivity2::class.java))
                        }


                )


                //status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween

                )
                {
                    var str:Int by remember { mutableStateOf(10) }
                    var agi:Int by remember { mutableStateOf(10) }
                    var int:Int by remember { mutableStateOf(10) }
                    Column() {
                        Button(onClick = {str = str+1 }) {
                            Text(text = "+", fontSize = 32.sp)
                        }

                        Text(text = "Str", fontSize = 32.sp)
                        Text(text = str.toString(), fontSize = 32.sp)
//                        Text(text ="-",fontSize=32.sp,
//                            modifier = Modifier.clickable {
//                                str = str-1
//                            })
                        Button(onClick = {str = str-1 }) {
                            Text(text = "-", fontSize = 32.sp)
                        }

                    }
                    Column() {
                        Button(onClick = {agi = agi+1 }) {
                            Text(text = "+", fontSize = 32.sp)
                        }

                        Text(text = "Agi", fontSize = 32.sp)
                        Text(text = agi.toString(), fontSize = 32.sp)

                        Button(onClick = {agi = agi-1 }) {
                            Text(text = "-", fontSize = 32.sp)
                        }
                    }
                    Column() {
                        Button(onClick = {int = int+1 }) {
                            Text(text = "+", fontSize = 32.sp)
                        }

                        Text(text = "Int", fontSize = 32.sp)
                        Text(text = int.toString(), fontSize = 32.sp)

                        Button(onClick = {int = int-1 }) {
                            Text(text = "-", fontSize = 32.sp)
                        }
                    }
                }


            }
        }
    }
    override fun onStart() {
        super.onStart()
        Log.i("Lifecycle", "MainActivity : onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.i("Lifecycle", "MainActivity : onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i("Lifecycle", "MainActivity : onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.i("Lifecycle", "MainActivity : onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("Lifecycle", "MainActivity : onDestroy")
    }

    override fun onRestart() {
        super.onRestart()
        Log.i("Lifecycle", "MainActivity : onRestart")
    }
}

//@Preview
//@Composable
//fun GreetingPreview() {
//    _517LabLearnAndroidTheme {
//        Greeting("Android")
//    }
//}
