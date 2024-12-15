package com.github.hbk01.poweramplyrics.lyric

interface LyricDownloader {
    fun download(artist: String, title: String, callback: (String) -> Unit)
}