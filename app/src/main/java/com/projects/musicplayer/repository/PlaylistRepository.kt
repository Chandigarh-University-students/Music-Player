package com.projects.musicplayer.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.projects.musicplayer.database.playlists.PlaylistConverter
import com.projects.musicplayer.database.playlists.PlaylistDao
import com.projects.musicplayer.database.playlists.PlaylistEntity
import com.projects.musicplayer.database.playlists.PlaylistRoomDatabase

class PlaylistRepository(application: Application) {

    private var mPlaylistDao: PlaylistDao

    val db: PlaylistRoomDatabase

    init {
        db = PlaylistRoomDatabase.getPlaylistDatabase(application)
        mPlaylistDao = db.playlistDao()
    }


    suspend fun createPlaylist(playlistEntity: PlaylistEntity) =
        mPlaylistDao.createPlaylist(playlistEntity)


    suspend fun deletePlaylist(playlistEntity: PlaylistEntity) =
        mPlaylistDao.deletePlaylist(playlistEntity)

    //return only id and name of playlist
    val allPlaylist: LiveData<List<PlaylistEntity>>
        get() {
            return mPlaylistDao.allPlaylist
        }

    suspend fun getPlaylistSongsById(id: Int): String? = mPlaylistDao.getPlaylistSongsById(id)
    suspend fun getAllPlaylistsId(): List<Int> = mPlaylistDao.getAllPlaylistsId()

    fun getPlaylistSongsByIdLive(id: Int): LiveData<String> = mPlaylistDao.getPlaylistSongsByIdLive(id)



    //pass entire list of songs whenever a new song is added/deleted from that individual playlist

    suspend fun updatePlaylist(id: Int, mSongs: List<Int>) {
        mPlaylistDao.updatePlaylist(id, PlaylistConverter.fromList(mSongs)!!)
    }
}