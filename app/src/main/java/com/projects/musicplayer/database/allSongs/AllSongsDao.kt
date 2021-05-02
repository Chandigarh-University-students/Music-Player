package com.projects.musicplayer.database.allSongs

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface AllSongsDao {
    //Inserting all songs for first time
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAfterFirstFetch(songsList: List<SongEntity>)

    @Insert
    suspend fun insert(song: SongEntity)

    // Deleting a song in case
    @Delete
    suspend fun removeSong(songEntity: SongEntity)

    //Retrieve all songs from the database
    @get:Query("SELECT * from all_songs_table")
    val allSongs: LiveData<List<SongEntity>>

    //Retrieve all fav songs from the database
    @get:Query("SELECT * from all_songs_table where isFav=1")
    val favSongs: LiveData<List<SongEntity>>

    //Get one song by it's id
    @Query(value = "SELECT * FROM all_songs_table WHERE songId = :id")
    suspend fun getSongById(id: Int): SongEntity

    //Check if this song is favorite
    @Query(value = "SELECT isFav FROM all_songs_table WHERE songId = :id")
    suspend fun checkFav(id: Int): Int

    //Update favorite for this song, either add or remove from fav
    @Query(value = "Update all_songs_table set isFav=isFav*(-1) WHERE songId=:id")
    suspend fun updateFav(id: Int)

    @Query(value = "Select * from all_songs_table")
    suspend fun getAllSongs():List<SongEntity>

}

