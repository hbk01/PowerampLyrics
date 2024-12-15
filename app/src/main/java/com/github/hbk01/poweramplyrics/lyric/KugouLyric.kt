package com.github.hbk01.poweramplyrics.lyric

import android.util.Base64
import android.util.Log
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.apache.commons.lang3.StringUtils
import org.json.JSONObject
import java.io.IOException
import java.net.URLEncoder
import kotlin.math.max

class KugouLyric : LyricDownloader {
    private val TAG = "KugouLyric"

    override fun download(artist: String, title: String, callback: (String) -> Unit) {
        search(artist, title) { hash ->
            list(hash) { id, key ->
                downloadLyric(id, key, callback)
            }
        }
    }

    /**
     * 搜索歌曲，获取歌曲 hash
     * @param artist 歌曲作者
     * @param title 歌曲标题
     * @param callback 回调
     * @author hbk01
     */
    private fun search(artist: String, title: String, callback: (String) -> Unit) {
        val keyword = URLEncoder.encode(artist + title, "utf-8")
        val searchUrl = "http://mobilecdn.kugou.com/api/v3/search/song?format=json&keyword=$keyword&page=1&pagesize=20&showtype=1"
        get(searchUrl) { data ->
            val jsonObject = JSONObject(data)
            val infos = jsonObject.getJSONObject("data").getJSONArray("info")

            var hash = infos.getJSONObject(0).getString("hash")
            var max = 0.0

            for (i in 0 until infos.length()) {
                val info = infos.getJSONObject(i)
                val singername = info.getString("singername")
                val filename = info.getString("filename")
                val similarity = calculateSimilarity("$singername$filename", "$artist$title")
                if (similarity > max) {
                    hash = info.getString("hash")
                    max = similarity
                }
            }
            Log.i(TAG, "search lyrics with song hash $hash")
            callback(hash)
        }
    }

    /**
     * 使用歌曲 hash，获取该歌曲拥有的歌词列表
     * @param hash 歌曲 hash
     * @param callback 回调
     * @author hbk01
     */
    private fun list(hash: String, callback: (String, String) -> Unit) {
        val listUrl = "http://krcs.kugou.com/search?ver=1&man=yes&client=mobi&hash=${hash}"
        get(listUrl) { data ->
            if (data == "") return@get
            val candidates = JSONObject(data).getJSONArray("candidates")
            var id = candidates.getJSONObject(0).getString("id")
            var key = candidates.getJSONObject(0).getString("accesskey")
            for (i in 0 until candidates.length()) {
                val candidate = candidates.getJSONObject(i)
                val from = candidate.getString("product_from")
                when (from) {
                    "官方推荐歌词" -> {
                        id = candidate.getString("id")
                        key = candidate.getString("accesskey")
                        break   // 跳出循环，优先使用官方推荐歌词
                    }

                    "第三方歌词" -> {
                        id = candidate.getString("id")
                        key = candidate.getString("accesskey")
                    }
                }
            }
            callback(id, key)
        }
    }

    /**
     * 使用歌词列表中包含的 id 和 accesskey 下载歌词
     * @param id id
     * @param key accesskey
     * @param callback 回调
     * @author hbk01
     */
    private fun downloadLyric(id: String, key: String, callback: (String) -> Unit) {
        val downloadUrl = "http://lyrics.kugou.com/download?ver=1&client=pc&id=$id&accesskey=$key&fmt=lrc&charset=utf8"
        get(downloadUrl) { data ->
            val content = JSONObject(data).getString("content")
            // Base64 解码 content 内容即为歌词
            val lyric = Base64.decode(content, Base64.DEFAULT).decodeToString()
            callback(lyric)
        }
    }

    /**
     * 发送 GET 请求
     *
     * @param url 地址
     * @param callback 回调
     * @author hbk01
     */
    private fun get(url: String, callback: (String) -> Unit) {
        val http = OkHttpClient()
        val searchRequest = Request.Builder()
            .url(url)
            .build()
        http.newCall(searchRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed get $url", e)
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body
                if (body != null) {
                    callback(body.string())
                } else {
                    callback("")
                }
            }
        })
    }

    /**
     * 计算两个字符串之间的相似度的方法
     */
    private fun calculateSimilarity(input: String, target: String): Double {
        // 计算两个字符串的最大长度
        val maxLength = max(input.length.toDouble(), target.length.toDouble()).toInt()
        // 使用 StringUtils 工具类计算 Levenshtein 距离
        val editDistance = StringUtils.getLevenshteinDistance(input, target)
        // 根据 Levenshtein 距离计算相似度
        return 1.0 - editDistance.toDouble() / maxLength
    }
}