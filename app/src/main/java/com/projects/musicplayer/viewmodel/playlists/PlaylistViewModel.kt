package com.projects.musicplayer.viewmodel.playlists

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projects.musicplayer.database.playlists.PlaylistEntity
import com.projects.musicplayer.repository.PlaylistRepository
import kotlinx.coroutines.launch

class PlaylistViewModel(application: Application) : ViewModel() {
    private val mPlaylistRepository: PlaylistRepository = PlaylistRepository(application)

    val allPlaylists: LiveData<List<PlaylistEntity>>
        get() = mPlaylistRepository.allPlaylist

    fun createPlaylist(playlistEntity: PlaylistEntity) {
        viewModelScope.launch {
            mPlaylistRepository.createPlaylist(playlistEntity)
        }
    }

    fun updatePlaylist(id: Int, mSongs: List<Int>) {
        viewModelScope.launch {
            mPlaylistRepository.updatePlaylist(id,mSongs)
        }
    }

    fun deletePlaylist(playlistEntity: PlaylistEntity) {
        viewModelScope.launch {
            mPlaylistRepository.deletePlaylist(playlistEntity)
        }
    }

    fun getPlaylistSongsByIdLive(id: Int): LiveData<String> =
        mPlaylistRepository.getPlaylistSongsByIdLive(id)

    suspend fun getPlaylistSongsById(id: Int): String? {
        return mPlaylistRepository.getPlaylistSongsById(id)
    }
}