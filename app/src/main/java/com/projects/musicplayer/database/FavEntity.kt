package com.projects.musicplayer.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fav_table")
data class FavEntity(
    @field:PrimaryKey val id: Int
)
