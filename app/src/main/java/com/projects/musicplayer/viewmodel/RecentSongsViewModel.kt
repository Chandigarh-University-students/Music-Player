package com.projects.musicplayer.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projects.musicplayer.database.RecentSongEntity
import com.projects.musicplayer.repository.RecentSongsRepository
import kotlinx.coroutines.launch

class RecentSongsViewModel(application: Application) : ViewModel() {
    private val mRecentSongsRepository: RecentSongsRepository = RecentSongsRepository(application)
    val recentSongs: LiveData<List<RecentSongEntity>>
        get() = mRecentSongsRepository.mRecentSongs


    fun insertAfterDeleteSong(song: RecentSongEntity) {
        //use of coroutine scope from viewModelScope
        viewModelScope.launch {
            mRecentSongsRepository.removeSong(song)
            mRecentSongsRepository.insertSong(song)
        }
    }
}