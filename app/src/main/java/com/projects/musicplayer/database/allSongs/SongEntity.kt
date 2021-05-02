package com.projects.musicplayer.database.allSongs

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "all_songs_table")
data class SongEntity(
    @field:PrimaryKey val songId: Int,
    @field:ColumnInfo(name = "songName") val songName: String,
    @field:ColumnInfo(name = "artistName") val artistName: String,
    @field:ColumnInfo(name = "duration") val duration: Long,
    @field:ColumnInfo(name = "albumCover") val albumId: String,
    @field:ColumnInfo(name = "isFav") var isFav: Int
)

