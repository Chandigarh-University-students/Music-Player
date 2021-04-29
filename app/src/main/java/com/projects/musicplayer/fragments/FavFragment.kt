package com.projects.musicplayer.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.projects.musicplayer.R
import com.projects.musicplayer.adapters.FavAdapter
import com.projects.musicplayer.database.RecentSongEntity
import com.projects.musicplayer.database.SongEntity
import com.projects.musicplayer.viewmodel.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class FavFragment : Fragment() {
    lateinit var toolbar: Toolbar
    lateinit var favRecyclerView: RecyclerView
    lateinit var favRecyclerViewAdapter: FavAdapter
    lateinit var emptyFavLayout: RelativeLayout

    //view model related
    private lateinit var mRecentSongsViewModel: RecentSongsViewModel
    private lateinit var mRecentSongsViewModelFactory: RecentSongsViewModelFactory
    private lateinit var mAllSongsViewModel: AllSongsViewModel
    private lateinit var mAllSongsViewModelFactory: AllSongsViewModelFactory
    private lateinit var mMediaControlViewModel: MediaControlViewModel

    private val uiscope = CoroutineScope(Dispatchers.Main)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** Viewmodel for MediaControl*/
        mMediaControlViewModel = ViewModelProvider(activity!!).get(MediaControlViewModel::class.java)

        /** Viewmodel for ALLSongs*/
        mAllSongsViewModelFactory = AllSongsViewModelFactory(activity!!.application)
        mAllSongsViewModel =
            ViewModelProvider(this, mAllSongsViewModelFactory).get(AllSongsViewModel::class.java)

        mAllSongsViewModel.favSongs.observe(viewLifecycleOwner, Observer {
            Log.i("LIVEDATAPLAYLISTUPDATE", "Setting all songs again in Favorites")

            if(it.isNullOrEmpty())
                emptyFavLayout.visibility = View.VISIBLE
            else
                emptyFavLayout.visibility = View.GONE

            uiscope.launch {
                    favRecyclerViewAdapter.setSongs(it)
            }
        })


        favRecyclerViewAdapter.favClickCallback = fun(id: Int) {
            //update fav whenever fav button clicked
            runBlocking {
                if(id==mMediaControlViewModel.nowPlayingSong.value?.songId){
                    /**This does not call any observer*/
                    /**This does not call any observer*/
                    mMediaControlViewModel.nowPlayingSong.value?.isFav  = mMediaControlViewModel.nowPlayingSong.value?.isFav?.times((-1))!!
                    Log.i("PLAYINGFAV","Value of nowPlaying is fav = ${mMediaControlViewModel.nowPlayingSong.value}")
                }
            }
            uiscope.launch {
                mAllSongsViewModel.updateFav(id)
            }
        }

        /** Viewmodel for RecentSongs*/
        mRecentSongsViewModelFactory = RecentSongsViewModelFactory(activity!!.application)
        mRecentSongsViewModel =
            ViewModelProvider(this, mRecentSongsViewModelFactory).get(RecentSongsViewModel::class.java)



        favRecyclerViewAdapter.onSongClickCallback = fun(recentSong: RecentSongEntity, song: SongEntity,allFavSongs:List<SongEntity>) {
            //update fav whenever fav button clicked
            uiscope.launch {
                mRecentSongsViewModel.insertAfterDeleteSong(recentSong)
                mMediaControlViewModel.nowPlayingSong.value = song
                mMediaControlViewModel.nowPlayingSongs.value=allFavSongs
                mMediaControlViewModel.nowPlaylist.value = "Favorites"
               Log.d("NOWPLAYING-VIEWMODEL", "Now Playing from HOME FRAGMENT $song updated")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_fav, container, false)

        toolbar=view.findViewById(R.id.favToolbar)
        favRecyclerView=view.findViewById(R.id.recyclerFavPlaylist)
        emptyFavLayout=view.findViewById(R.id.emptyFavLayout)

        if (activity != null){
           toolbar.title = "Favorites"

            emptyFavLayout.visibility = View.GONE

            favRecyclerViewAdapter= FavAdapter(activity as Context)
            favRecyclerView.adapter=favRecyclerViewAdapter
            favRecyclerView.layoutManager= LinearLayoutManager(activity)
            favRecyclerView.addItemDecoration(
                DividerItemDecoration(
                    favRecyclerView.context,
                    (favRecyclerView.layoutManager as LinearLayoutManager).orientation
                )
            )
        }

        return view
    }

}