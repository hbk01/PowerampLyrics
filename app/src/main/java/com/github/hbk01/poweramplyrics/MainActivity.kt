package com.github.hbk01.poweramplyrics

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.hbk01.poweramplyrics.ui.theme.PowerampLyricsTheme
import com.maxmpz.poweramp.player.PowerampAPI
import com.maxmpz.poweramp.player.PowerampAPI.NO_ID
import com.maxmpz.poweramp.player.PowerampAPIHelper


class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private val LOG_VERBOSE = true
    private val FORCE_API_ACTIVITY = true
    private var sPermissionAsked = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (LOG_VERBOSE) Log.w(TAG, "onCreate")

        // Ask Poweramp for a permission to access its data provider. Needed only if we want to make queries against Poweramp database, e.g. in FilesActivity/FoldersActivity
        // NOTE: this will work only if Poweramp process is alive.
        // This actually should be done once per this app installation, but for the simplicity, we use per-process static field here
        if (!sPermissionAsked) {
            if (LOG_VERBOSE) Log.w(TAG, "onCreate request poweramp permission")
            val intent = Intent(PowerampAPI.ACTION_ASK_FOR_DATA_PERMISSION)
            intent.setPackage(PowerampAPIHelper.getPowerampPackageName(this))
            intent.putExtra(PowerampAPI.EXTRA_PACKAGE, packageName)
            if (FORCE_API_ACTIVITY) {
                intent.setComponent(PowerampAPIHelper.getApiActivityComponentName(this))
                startActivitySafe(intent)
            } else {
                sendBroadcast(intent)
            }
            sPermissionAsked = true
        }

        val intent = intent
        if(intent != null && intent.action == PowerampAPI.Lyrics.ACTION_LYRICS_LINK
            && !intent.getBooleanExtra("__processed", false)
        ) {
            intent.putExtra("__processed", true)
            handleLyricsLinkIntent(intent)
        }

        setContent {
            PowerampLyricsTheme {
                Greeting(
                    name = PowerampAPIHelper.getPowerampBuild(this).toString(),
                )
            }
        }
    }

    private fun handleLyricsLinkIntent(intent: Intent) {
        onNewIntent(intent)
        if(LOG_VERBOSE) Log.w(TAG, "onNewIntent")
        val msg: String
        val realId = intent.getLongExtra(PowerampAPI.Track.REAL_ID, NO_ID)
        msg = if(realId != NO_ID) {
            """
                REAL_ID=$realId
                TITLE=${intent.getStringExtra(PowerampAPI.Track.TITLE)}
                ALBUM=${intent.getStringExtra(PowerampAPI.Track.ALBUM)}
                ARTIST=${intent.getStringExtra(PowerampAPI.Track.ARTIST)}
                TYPE=${intent.getStringExtra(PowerampAPI.Track.FILE_TYPE)}
                DURATION_MS=${intent.getIntExtra(PowerampAPI.Track.DURATION_MS, 0)}
                """.trimIndent()
        } else {
            "No track info provided"
        }

        AlertDialog.Builder(this)
            .setTitle("ACTION_LYRICS_LINK")
            .setMessage(msg)
            .setPositiveButton(android.R.string.ok, null)
            .show()

    }

    private fun startActivitySafe(intent: Intent) {
        try {
            if (LOG_VERBOSE) Log.w(TAG, "startActivity=$intent")
            startActivity(intent)
        } catch (th: Throwable) {
            Log.e(TAG, "FAIL intent=$intent", th);
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

