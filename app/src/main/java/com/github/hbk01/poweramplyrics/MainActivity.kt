package com.github.hbk01.poweramplyrics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.hbk01.poweramplyrics.ui.theme.PowerampLyricsTheme
import com.maxmpz.poweramp.player.PowerampAPIHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PowerampLyricsTheme {
                Greeting(
                    name = PowerampAPIHelper.getPowerampBuild(this).toString(),
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(
        text = "Poweramp version $name",
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