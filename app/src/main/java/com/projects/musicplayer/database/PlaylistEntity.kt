package com.projects.musicplayer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "playlist_table")
data class PlaylistEntity(
    @field:PrimaryKey val id: Int,
    @field:ColumnInfo(name = "name") val name: String
//    , @field:ColumnInfo(name = "songs") val songs: List<Int>
)
