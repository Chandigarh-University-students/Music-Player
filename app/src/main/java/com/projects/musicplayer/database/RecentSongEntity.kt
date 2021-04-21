package com.projects.musicplayer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_songs_table")
data class RecentSongEntity(
    @field:PrimaryKey val songId: Int,
    @field:ColumnInfo(name = "lastPlayed") val lastPlayed: Long //check -> [Set to long by using type converters]
)

