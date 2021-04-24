package com.projects.musicplayer.rest

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fav_table")
data class FavEntity(
    @field:PrimaryKey val id: Int
)
