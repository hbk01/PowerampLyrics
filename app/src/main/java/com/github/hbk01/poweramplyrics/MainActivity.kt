package com.github.hbk01.poweramplyrics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.hbk01.poweramplyrics.ui.theme.PowerampLyricsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PowerampLyricsTheme {
                Greeting(
                    name = "Android",
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(
        text = "Hello $name!",
        modifier = Modifier.padding(32.dp, 12.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PowerampLyricsTheme {
        Greeting("Android")
    }
}