package com.projects.musicplayer.rest

import androidx.lifecycle.LiveData
import androidx.room.*
import com.projects.musicplayer.rest.FavEntity

@Dao
interface FavDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToFav(favEntity: FavEntity)

    @Delete
    suspend fun removeFromFav(favEntity: FavEntity)

    @get:Query("SELECT * from fav_table")
    val allFav: LiveData<List<FavEntity>>

    @Query(value = "SELECT COUNT(*) FROM fav_table WHERE id = :id")
    fun checkFav(id: Int): LiveData<Int> //TODO What to return?
}