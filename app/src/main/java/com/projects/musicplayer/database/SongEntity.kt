package com.projects.musicplayer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "all_songs_table")
data class SongEntity(
    @field:PrimaryKey val songId: Int,
    @field:ColumnInfo(name = "songName") val songName: String,
    @field:ColumnInfo(name = "artistName") val artistName: String,
    @field:ColumnInfo(name = "duration") val duration: Long, //check -> [Set to long as milliseconds]
    @field:ColumnInfo(name = "albumCover") val albumCover: String, //check
    @field:ColumnInfo(name = "isFav") var isFav: Int
)

