package com.projects.musicplayer.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.projects.musicplayer.R
import com.projects.musicplayer.adapters.SinglePlaylistAdapter
import com.projects.musicplayer.database.RecentSongEntity
import com.projects.musicplayer.viewmodel.AllSongsViewModel
import com.projects.musicplayer.viewmodel.AllSongsViewModelFactory
import com.projects.musicplayer.viewmodel.RecentSongsViewModel
import com.projects.musicplayer.viewmodel.RecentSongsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SinglePlaylistFragment : Fragment() {
    lateinit var toolbar:Toolbar
    lateinit var singlePlaylistRecyclerView:RecyclerView
    lateinit var singlePlaylistRecyclerViewAdapter: SinglePlaylistAdapter

    //view model related //TODO Check
//    private lateinit var mSinglePlaylistViewModel: SinglePlaylistViewModel
//    private lateinit var mSinglePlaylistViewModelFactory: SinglePlaylistViewModelFactory
    private lateinit var mRecentSongsViewModel: RecentSongsViewModel
    private lateinit var mRecentSongsViewModelFactory: RecentSongsViewModelFactory
    private val uiscope = CoroutineScope(Dispatchers.Main)

//TODO ViewModel for single playlist
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)/*
        mAllSongsViewModelFactory = AllSongsViewModelFactory(activity!!.application)
        mAllSongsViewModel =
            ViewModelProvider(this, mAllSongsViewModelFactory).get(AllSongsViewModel::class.java)

        mAllSongsViewModel.allSongs.observe(viewLifecycleOwner, Observer {
            Log.i("LIVEDATA-UPDATE","Setting all songs again")
            adapterAllSongs.setSongs(it!!)
        })
*/
        singlePlaylistRecyclerViewAdapter.favClickCallback = fun(id: Int) {
            //update fav whenever fav button clicked
            uiscope.launch {
                //mSinglePlaylistViewModel.updateFav(id)
            }

            //TODO for adding to recent tracks
            mRecentSongsViewModelFactory = RecentSongsViewModelFactory(activity!!.application)
            mRecentSongsViewModel =
                ViewModelProvider(this, mRecentSongsViewModelFactory).get(RecentSongsViewModel::class.java)

            /**Nothing to observe in this fragment for recent song*/
       /*     mRecentSongsViewModel.recentSongs.observe(viewLifecycleOwner, Observer {
                Log.i("LIVEDATA-UPDATE","Setting recent songs again")//TODO continue
                adapterRecentTracks.addTracks(it!!)
            })*/

            singlePlaylistRecyclerViewAdapter.onSongClickCallback = fun(song: RecentSongEntity) {
                //update fav whenever fav button clicked
                uiscope.launch {
                    mRecentSongsViewModel.insertAfterDeleteSong(song)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view=inflater.inflate(R.layout.fragment_single_playlist, container, false)

        toolbar=view.findViewById(R.id.SinglePlaylistToolbar)
        singlePlaylistRecyclerView=view.findViewById(R.id.recyclerViewSinglePlaylist)
        toolbar.title="Playlist Name" //TODO Can be either of three things, logic to set

        if (activity != null){
            singlePlaylistRecyclerViewAdapter= SinglePlaylistAdapter(activity as Context)
            singlePlaylistRecyclerView.adapter=singlePlaylistRecyclerViewAdapter
            singlePlaylistRecyclerView.layoutManager= LinearLayoutManager(activity)
            singlePlaylistRecyclerView.addItemDecoration(
                DividerItemDecoration(
                singlePlaylistRecyclerView.context,
                (singlePlaylistRecyclerView.layoutManager as LinearLayoutManager).orientation
            )
            )

        }
        return view
    }
}