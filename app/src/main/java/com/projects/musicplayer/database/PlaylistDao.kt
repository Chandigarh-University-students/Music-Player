package com.projects.musicplayer.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun createPlaylist(playlistEntity: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlistEntity: PlaylistEntity)

    //return only id and name of playlist
    @get:Query("SELECT * from playlist_table ORDER BY  name ASC")
    val allPlaylist: LiveData<List<PlaylistEntity>>

//    //get all song_id in playlist of given playlist_id
//    @Query("SELECT songs from playlist_table WHERE id = :id")
//    fun getPlaylistSongsById(id: Int): LiveData<List<Int>>
//
//    //pass entire list of songs whenever a new song is added/deleted from that individual playlist
//    @Query("UPDATE playlist_table SET songs=:mSongs WHERE id = :id")
//    suspend fun updatePlaylist(id: Int, mSongs: List<Int>)

//get all song_id in playlist of given playlist_id
    @Query("SELECT songs from playlist_table WHERE id = :id")
    fun getPlaylistSongsById(id: Int): LiveData<String>

    //pass entire list of songs whenever a new song is added/deleted from that individual playlist
    @Query("UPDATE playlist_table SET songs=:mSongs WHERE id = :id")
    suspend fun updatePlaylist(id: Int, mSongs: String)

}