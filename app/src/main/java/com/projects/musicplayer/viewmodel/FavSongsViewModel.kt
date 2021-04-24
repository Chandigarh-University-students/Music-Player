package com.projects.musicplayer.viewmodel

import android.app.Application
import android.util.Log
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
    }

    fun checkFav(id: Int): Boolean
    {
        var isFav=false
        Log.e("FAVE",mFavSongsRepository.checkFav(id).toString());
        if(mFavSongsRepository.checkFav(id).value!=0)
            isFav=true
        return isFav
    }


}
