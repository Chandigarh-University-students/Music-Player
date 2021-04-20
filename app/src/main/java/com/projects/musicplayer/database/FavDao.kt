package com.projects.musicplayer.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FavDao {
    @Insert
    suspend fun addToFav(favEntity: FavEntity)

    @Delete
    suspend fun removeFromFav(favEntity: FavEntity)

    @get:Query("SELECT * from fav_table")
    val allFav: LiveData<List<FavEntity>>

    @Query(value = "SELECT * FROM fav_table WHERE id = :id")
    suspend fun checkFav(id: String): FavEntity
}