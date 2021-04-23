package com.projects.musicplayer.viewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projects.musicplayer.database.FavEntity
import com.projects.musicplayer.database.SongEntity
import com.projects.musicplayer.repository.FavSongsRepository
import kotlinx.coroutines.launch

class FavSongsViewModel(application: Application) : ViewModel() {
    private val mFavSongsRepository: FavSongsRepository = FavSongsRepository(application)
    val favSongs: LiveData<List<FavEntity>>
        get() = mFavSongsRepository.mAllFavSongs


    fun insertSong(song: FavEntity) {
        //use of coroutine scope from viewModelScope
        viewModelScope.launch {
            mFavSongsRepository.insertSong(song)
        }
    }

    fun removeSong(song: FavEntity) {
        //use of coroutine scope from viewModelScope
        viewModelScope.launch {
            mFavSongsRepository.removeSong(song)
        }

        fun checkFav(id: Int): Int {
            var isFav = -1
            var listOfID : List<FavEntity>
            viewModelScope.launch {
                listOfID= mFavSongsRepository.checkFav(id)
                if(listOfID.isNotEmpty())
                    isFav=1
            }
            return isFav
        }

    }
}