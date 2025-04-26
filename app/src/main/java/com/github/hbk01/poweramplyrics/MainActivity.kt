package com.github.hbk01.poweramplyrics

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.hbk01.poweramplyrics.ui.theme.PowerampLyricsTheme
import com.maxmpz.poweramp.player.PowerampAPIHelper


class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        setContent {
            PowerampLyricsTheme {
                Column(modifier = Modifier.padding(10.dp)) {
                    ShowPowerampPkg(this@MainActivity)
                    HorizontalDivider()
                    ShowHistory(this@MainActivity)
                }
            }
        }
    }
}

@Composable
fun ShowHistory(ctx: Context) {
    val sp = ctx.getSharedPreferences("history", Context.MODE_PRIVATE)
    val list = sp.all.keys.toMutableStateList()
    Column {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(list) {
                HistoryLine(it, sp.getString(it, "").toString())
            }
        }

    }
}

@Composable
fun HistoryLine(time: String, title: String) {
    Card (
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 5.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
    ) {
        Text(text = time, modifier = Modifier.padding(start = 10.dp).height(32.dp))
        Text(text = title, modifier = Modifier.padding(start = 10.dp).height(32.dp))
    }
}

@Composable
fun ShowPowerampPkg(ctx: Context) {
    val packageName = PowerampAPIHelper.getPowerampPackageName(ctx)
    val packageVersion = PowerampAPIHelper.getPowerampBuild(ctx)
    Column {
        Text("Poweramp Package: $packageName")
        Text("Poweramp Build: $packageVersion")
    }

}

