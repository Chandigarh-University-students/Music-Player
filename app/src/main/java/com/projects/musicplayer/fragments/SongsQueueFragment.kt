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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.projects.musicplayer.R
import com.projects.musicplayer.adapters.SinglePlaylistAdapter
import com.projects.musicplayer.adapters.SongQueueAdapter
import com.projects.musicplayer.database.PlaylistConverter
import com.projects.musicplayer.database.RecentSongEntity
import com.projects.musicplayer.database.SongEntity
import com.projects.musicplayer.rest.FavSongsViewModel
import com.projects.musicplayer.rest.FavSongsViewModelFactory
import com.projects.musicplayer.viewmodel.*
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
        mAllSongsViewModelFactory = AllSongsViewModelFactory(activity!!.application)
        mAllSongsViewModel =
            ViewModelProvider(this, mAllSongsViewModelFactory).get(AllSongsViewModel::class.java)

        /** Viewmodel for RecentSongs*/
        mRecentSongsViewModelFactory = RecentSongsViewModelFactory(activity!!.application)
        mRecentSongsViewModel = ViewModelProvider(this, mRecentSongsViewModelFactory).get(RecentSongsViewModel::class.java)

        /** Viewmodel for MediaControl*/
        mMediaControlViewModel = ViewModelProvider(activity!!).get(MediaControlViewModel::class.java)

        mMediaControlViewModel.nowPlayingSongs.observe(viewLifecycleOwner, Observer {
            songQueueRecyclerViewAdapter.setSongs(it)
            Log.i("SONGQUEUE",it.toString())
        })

        mMediaControlViewModel.nowPlaylist.observe(viewLifecycleOwner, Observer {
            toolbar.title=it
            Log.i("SONGQUEUETITLE","Songs title $it set in queue")
        })

        mMediaControlViewModel.nowPlayingSong.observe(viewLifecycleOwner, Observer {
            //TODO to show current playing song as selected
            songQueueRecyclerViewAdapter.setSongs(mMediaControlViewModel.nowPlayingSongs.value!!)
        })

       /* mAllSongsViewModel.allSongs.observe(viewLifecycleOwner, Observer {
            mMediaControlViewModel.nowPlayingSongs.value=it
        })*/

        songQueueRecyclerViewAdapter.currentPlayingSetSelected = fun(currentSong:SongEntity,cardViewForSong:RelativeLayout){
            if(currentSong==mMediaControlViewModel.nowPlayingSong.value){
                Log.i("PLAYING","Change color for ${currentSong.songName}")
                val color = resources.getColor(R.color.secondaryColor)
                cardViewForSong.setBackgroundColor(color)
                //cardViewForSong.cardElevation= 80F
            }
            else{
                val color = resources.getColor(R.color.backgroundColor)
                cardViewForSong.setBackgroundColor(color)
            }
        }

        songQueueRecyclerViewAdapter.favClickCallback = fun(id: Int) {
            //update fav whenever fav button clicked
            uiscope.launch {
                //TODO add to favourites both places
                mAllSongsViewModel.updateFav(id)
                //TODO use observer to update in MediaViewModel too
            }
        }


        songQueueRecyclerViewAdapter.onSongClickCallback = fun(recentSong: RecentSongEntity,song:SongEntity,allSongs:List<SongEntity>) {
            //update fav whenever fav button clicked
            uiscope.launch {
                //TODO both play song and add to recent
                mRecentSongsViewModel.insertAfterDeleteSong(recentSong)
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
            //TODO set from viewmodel
            //toolbar.title = playlistName

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