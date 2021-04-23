package com.projects.musicplayer.rest

data class Song(val songName: String, val artistName: String, var isFav: Boolean = false)

