package com.projects.musicplayer.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PlaylistDao {
    @Insert
    suspend fun createPlaylist(playlistEntity: PlaylistEntity)

    @Delete
    suspend fun deletePlaylist(playlistEntity: PlaylistEntity)

    //return only id and name of playlist
    @get:Query("SELECT id,name from playlist_table")
    val allPlaylist: LiveData<List<FavEntity>>

    //get all song_id in playlist of given playlist_id
    @Query("SELECT songs from playlist_table WHERE id = :id")
    fun getPlaylistSongsById(id: String): LiveData<List<Int>>

    //pass entire list of songs whenever a new song is added/deleted from that individual playlist
    @Query("UPDATE playlist_table SET songs=:mSongs WHERE id = :id")
    suspend fun updatePlaylist(id: String, mSongs: List<Int>)

}