package com.projects.musicplayer.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projects.musicplayer.database.PlaylistEntity
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


    fun deletePlaylist(playlistEntity: PlaylistEntity) {
        viewModelScope.launch {
            mPlaylistRepository.deletePlaylist(playlistEntity)
        }
    }

    fun getPlaylistSongsById(id: Int): LiveData<String> =
        mPlaylistRepository.getPlaylistSongsById(id)


}