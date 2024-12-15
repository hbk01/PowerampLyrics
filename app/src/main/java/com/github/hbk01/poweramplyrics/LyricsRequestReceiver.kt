// This file was modified from here:
// https://github.com/maxmpz/powerampapi/blob/master/poweramp_lyrics_plugin_example/src/main/java/com/maxmpz/poweramplyricspluginexample/LyricsRequestReceiver.kt

/*
Copyright (C) 2011-2023 Maksim Petrov

Redistribution and use in source and binary forms, with or without
modification, are permitted for widgets, plugins, applications and other software
which communicate with Poweramp application on Android platform.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.github.hbk01.poweramplyrics

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.github.hbk01.poweramplyrics.lyric.KugouLyric
import com.maxmpz.poweramp.player.PowerampAPI
import com.maxmpz.poweramp.player.PowerampAPI.NO_ID
import com.maxmpz.poweramp.player.PowerampAPIHelper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * 歌词请求广播接收器
 *
 * 接受来自 Poweramp 的歌词请求（通过广播）
 */
class LyricsRequestReceiver : BroadcastReceiver() {
    private val TAG = "LyricsRequestReceiver"
    private val LOG = true

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == PowerampAPI.Lyrics.ACTION_NEED_LYRICS) {
            // Extract few extras, such as real track id, album, artist, title, duration, so we're able to do a "search".
            // RealId is required for the response.
            // Title is generally usually required as well
            val realId = intent.getLongExtra(PowerampAPI.Track.REAL_ID, NO_ID)
            val title = intent.getStringExtra(PowerampAPI.Track.TITLE)

            if (realId == NO_ID || title.isNullOrEmpty()) {
                Log.e(TAG, "FAIL readId=$realId title=$title")
                return
            }

            // NOTE: album/artist can be "" (empty string) for the unknown album/artist
            val album = intent.getStringExtra(PowerampAPI.Track.ALBUM)
            val artist = intent.getStringExtra(PowerampAPI.Track.ARTIST)
            var durationMs = intent.getIntExtra(PowerampAPI.Track.DURATION_MS, 0)

            // We can extract other PowerampAPI.Track fields from extras here if needed
            val fileType = intent.getIntExtra(PowerampAPI.Track.FILE_TYPE, PowerampAPI.Track.FileType.TYPE_UNKNOWN)

            // NOTE: if it's a stream, we won't get durationMs and it may be generally harder to guess/search/load
            // lyrics for such stream "tracks"
            val isStream = fileType == PowerampAPI.Track.FileType.TYPE_STREAM
            if (isStream) return // so, we won't load lyrics for stream tracks

            val debugLine = "ACTION_NEED_LYRICS realId=$realId title=$title album=$album artist=$artist durationMs=$durationMs"
            if (LOG) Log.w(TAG, debugLine)

            val infoLine = "Poweramp Lyrics"

            GlobalScope.launch(Dispatchers.IO) {
                KugouLyric().download(artist.toString(), title) { lyric ->
                    sendLyricsResponse(context, realId, lyric, infoLine)
                }
            }
        }
    }

    /**
     * Sends lyrics response for given [realId] track
     * @param lyrics in LRC or plain text format. If null, we indicate a failure to load
     * @return true if we sent it, false on failure
     */
    fun sendLyricsResponse(context: Context, realId: Long, lyrics: String?, infoLine: String?): Boolean {
        if (LOG) Log.w(TAG, "sendLyricsResponse realId=$realId infoLine=$infoLine")
        val intent = Intent(PowerampAPI.Lyrics.ACTION_UPDATE_LYRICS)
        intent.putExtra(PowerampAPI.EXTRA_ID, realId)
        intent.putExtra(PowerampAPI.Lyrics.EXTRA_LYRICS, lyrics) // Can be null
        intent.putExtra(PowerampAPI.Lyrics.EXTRA_INFO_LINE, infoLine) // Can be null
        try {
            PowerampAPIHelper.sendPAIntent(context, intent)
            val debugLine = "sendLyricsResponse realId=$realId"
            if (LOG) Log.w(TAG, debugLine)
            return true
        } catch (th: Throwable) {
            Log.e(TAG, "Failed to send lyrics response", th)
        }
        return false
    }
}