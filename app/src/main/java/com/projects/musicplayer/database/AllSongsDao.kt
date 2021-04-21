package com.projects.musicplayer.database

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface AllSongsDao {
    //Inserting all songs for first time
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAfterFirstFetch(songsList: List<SongEntity>)

    // Deleting a song in case
    @Delete
    suspend fun removeSong(songEntity: SongEntity)

    //Retreive all songs from the database
    @get:Query("SELECT * from all_songs_table")
    val allSongs: LiveData<List<SongEntity>>

    //Get one song by it's id
    @Query(value = "SELECT * FROM all_songs_table WHERE songId = :id")
    suspend fun getSongsById(id: String): FavEntity

    //Check if this song is favorite
    @Query(value = "SELECT isFav FROM all_songs_table WHERE songId = :id")
    suspend fun checkFav(id: String): SongEntity

    //Update favorite for this song, either add or remove from fav
    @Query(value = "Update all_songs_table set isFav=isFav*(-1)")
    suspend fun updateFav()

}

