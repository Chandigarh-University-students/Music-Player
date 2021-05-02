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
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.projects.musicplayer.R
import com.projects.musicplayer.adapters.SongQueueAdapter
import com.projects.musicplayer.database.recentSongs.RecentSongEntity
import com.projects.musicplayer.database.allSongs.SongEntity
import com.projects.musicplayer.viewmodel.allSongs.AllSongsViewModel
import com.projects.musicplayer.viewmodel.allSongs.AllSongsViewModelFactory
import com.projects.musicplayer.viewmodel.mediaControl.MediaControlViewModel
import com.projects.musicplayer.viewmodel.recentSongs.RecentSongsViewModel
import com.projects.musicplayer.viewmodel.recentSongs.RecentSongsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class SongsQueueFragment : Fragment() {

    lateinit var toolbar: Toolbar
    lateinit var songQueueRecyclerView: RecyclerView
    lateinit var songQueueRecyclerViewAdapter: SongQueueAdapter

    //viewmode
    private lateinit var mMediaControlViewModel: MediaControlViewModel
    private lateinit var mAllSongsViewModel: AllSongsViewModel
    private lateinit var mAllSongsViewModelFactory: AllSongsViewModelFactory
    private lateinit var mRecentSongsViewModel: RecentSongsViewModel
    private lateinit var mRecentSongsViewModelFactory: RecentSongsViewModelFactory

    private val uiscope = CoroutineScope(Dispatchers.Main)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** Viewmodel for ALLSongs*/
        mAllSongsViewModelFactory =
            AllSongsViewModelFactory(
                activity!!.application
            )
        mAllSongsViewModel =
            ViewModelProvider(this, mAllSongsViewModelFactory).get(AllSongsViewModel::class.java)

        /** Viewmodel for RecentSongs*/
        mRecentSongsViewModelFactory =
            RecentSongsViewModelFactory(
                activity!!.application
            )
        mRecentSongsViewModel = ViewModelProvider(this, mRecentSongsViewModelFactory).get(
            RecentSongsViewModel::class.java)

        /** Viewmodel for MediaControl*/
        mMediaControlViewModel = ViewModelProvider(activity!!).get(MediaControlViewModel::class.java)

        mMediaControlViewModel.nowPlayingSongs.observe(viewLifecycleOwner, Observer {
            songQueueRecyclerViewAdapter.setSongs(it)
            Log.i("SONGQUEUE",it.toString())
        })

        mMediaControlViewModel.nowPlaylist.observe(viewLifecycleOwner, Observer {
            Log.i("SONGQUEUETITLE","Songs title $it set in queue")
        })

        mMediaControlViewModel.nowPlayingSong.observe(viewLifecycleOwner, Observer {
            songQueueRecyclerViewAdapter.setSongs(mMediaControlViewModel.nowPlayingSongs.value!!)
        })

        songQueueRecyclerViewAdapter.currentPlayingSetSelected = fun(currentSong: SongEntity, cardViewForSong:RelativeLayout, cardView:CardView){
            Log.i("PLAYING","Value of current Song = ${currentSong.songId}")
            Log.i("PLAYING","Value of nowPlayingSong = ${mMediaControlViewModel.nowPlayingSong.value?.songId}")
            Log.i("PLAYING","Value of boolean = ${currentSong.songId==mMediaControlViewModel.nowPlayingSong.value?.songId}")
            if(currentSong.songId==mMediaControlViewModel.nowPlayingSong.value?.songId){
                Log.i("PLAYING","Change color for ${currentSong.songName}")
                cardViewForSong.setBackgroundColor(ContextCompat.getColor(activity as Context,R.color.secondaryColor))
            }
            else{
                val color = resources.getColor(R.color.backgroundColor)
                cardViewForSong.setBackgroundColor(color)
            }
        }

        songQueueRecyclerViewAdapter.favClickCallback = fun(id: Int) {
            //update fav whenever fav button clicked
            runBlocking {
                if(id==mMediaControlViewModel.nowPlayingSong.value?.songId){
                    /**This does not call any observer*/
                    mMediaControlViewModel.nowPlayingSong.value?.isFav  = mMediaControlViewModel.nowPlayingSong.value?.isFav?.times((-1))!!
                    Log.i("PLAYINGFAV","Value of nowPlaying is fav = ${mMediaControlViewModel.nowPlayingSong.value}")
                }
            }
            uiscope.launch {
                mAllSongsViewModel.updateFav(id)
            }
        }


        songQueueRecyclerViewAdapter.onSongClickCallback = fun(song: SongEntity, allSongs:List<SongEntity>) {
            //update fav whenever fav button clicked
            uiscope.launch {
                mMediaControlViewModel.nowPlayingSong.value = song
            }
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_songs_queue, container, false)

        toolbar=view.findViewById(R.id.songQueueToolbar)
        songQueueRecyclerView=view.findViewById(R.id.recyclerSongQueue)
        Log.e("SONGQUEUE","SongsQueueFragment creating")

        if (activity != null){
            // set this playlist according to which fragment called it
            toolbar.title = "Play Queue"

            songQueueRecyclerViewAdapter= SongQueueAdapter(activity as Context)
            songQueueRecyclerView.adapter= songQueueRecyclerViewAdapter
            songQueueRecyclerView.layoutManager= LinearLayoutManager(activity)
            songQueueRecyclerView.addItemDecoration(
                DividerItemDecoration(
                    songQueueRecyclerView.context,
                    (songQueueRecyclerView.layoutManager as LinearLayoutManager).orientation
                )
            )
        }
        return view
    }
}