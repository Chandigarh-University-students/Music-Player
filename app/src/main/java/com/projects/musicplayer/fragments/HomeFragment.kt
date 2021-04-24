package com.projects.musicplayer.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.projects.musicplayer.adapters.AllSongsAapter
import com.projects.musicplayer.R
import com.projects.musicplayer.adapters.RecentTracksAdapter
import com.projects.musicplayer.database.FavEntity
import com.projects.musicplayer.database.RecentSongEntity
import com.projects.musicplayer.database.SongEntity
import com.projects.musicplayer.viewmodel.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    lateinit var recyclerViewAllSongs: RecyclerView
    lateinit var adapterAllSongs: AllSongsAapter

    lateinit var recyclerViewRecentTracks: RecyclerView
    lateinit var adapterRecentTracks: RecentTracksAdapter

    //view model related
    private lateinit var mAllSongsViewModel: AllSongsViewModel
    private lateinit var mAllSongsViewModelFactory: AllSongsViewModelFactory
    private lateinit var mRecentSongsViewModel: RecentSongsViewModel
    private lateinit var mRecentSongsViewModelFactory: RecentSongsViewModelFactory
    private lateinit var mFavSongsViewModel: FavSongsViewModel
    private lateinit var mFavSongsViewModelFactory: FavSongsViewModelFactory


    private val uiscope = CoroutineScope(Dispatchers.Main)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /**ViewModel for ALLSongs*/
        mAllSongsViewModelFactory = AllSongsViewModelFactory(activity!!.application)
        mAllSongsViewModel =
            ViewModelProvider(this, mAllSongsViewModelFactory).get(AllSongsViewModel::class.java)

        /**ViewModel for FavSongs*/
        mFavSongsViewModelFactory = FavSongsViewModelFactory(activity!!.application)
        mFavSongsViewModel =
            ViewModelProvider(this, mFavSongsViewModelFactory).get(FavSongsViewModel::class.java)


        mAllSongsViewModel.allSongs.observe(viewLifecycleOwner, Observer {
            Log.i("LIVEDATA-UPDATE","Setting all songs again")
            adapterAllSongs.setSongs(it!!)
        })

        adapterAllSongs.favClickCallback = fun(id: Int) {
            //update fav whenever fav button clicked
            uiscope.launch {

              /*  val favSongs = mFavSongsViewModel.favSongs.value
                Log.e("FAV",favSongs.toString())
                if(favSongs!=null){
                    if(FavEntity(id) in favSongs)
                        mFavSongsViewModel.removeSong(FavEntity(id))
                    else
                        mFavSongsViewModel.insertSong(FavEntity(id))
                }else{
                    mFavSongsViewModel.insertSong(FavEntity(id))
                }
                Log.e("FAV",mFavSongsViewModel.checkFav(id).toString())
                if(mFavSongsViewModel.checkFav(id))
                    mFavSongsViewModel.removeSong(FavEntity(id))
                else
                    mFavSongsViewModel.insertSong(FavEntity(id))*/

                //TODO update both databases fav and allsongs
                mAllSongsViewModel.updateFav(id)

               /* if(!mFavSongsViewModel.checkFav(id)){
                    Log.e("INSERT", "Insert $id")
                      mFavSongsViewModel.insertSong(FavEntity(id))
                }
                else{
                    Log.e("REMOVE", "Remove $id")
                    mFavSongsViewModel.removeSong(FavEntity(id))
                }*/
            }
        }


        /**ViewModel for RecentSongs*/
        mRecentSongsViewModelFactory = RecentSongsViewModelFactory(activity!!.application)
        mRecentSongsViewModel =
            ViewModelProvider(this, mRecentSongsViewModelFactory).get(RecentSongsViewModel::class.java)

        mRecentSongsViewModel.recentSongs.observe(viewLifecycleOwner, Observer {
            Log.i("LIVEDATA-UPDATE","Setting recent songs again")//TODO continue
            adapterRecentTracks.addTracks(it!!)
        })

        adapterAllSongs.onSongClickCallback = fun(recentSong: RecentSongEntity,song: SongEntity) {
            //update recent tracks
            uiscope.launch {
                //TODO both play song and add to recent
                mRecentSongsViewModel.insertAfterDeleteSong(recentSong)
            }
        }

        adapterRecentTracks.onSongClickCallback = fun(song: RecentSongEntity) {
            //update recent tracks
            uiscope.launch {
                //TODO both play song and add to recent
                mRecentSongsViewModel.insertAfterDeleteSong(song)
                //TODO play here using id
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerViewAllSongs = view.findViewById(R.id.recyclerAllSongs)
        recyclerViewRecentTracks = view.findViewById(R.id.recyclerRecentTrack)


        if (activity != null) {
            adapterAllSongs =
                AllSongsAapter(activity as Context)
            adapterRecentTracks =
                RecentTracksAdapter(activity as Context)

            recyclerViewAllSongs.adapter = adapterAllSongs
            recyclerViewRecentTracks.adapter = adapterRecentTracks

            recyclerViewAllSongs.layoutManager = LinearLayoutManager(activity)
            val horizontalLayoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recyclerViewRecentTracks.layoutManager = horizontalLayoutManager

            recyclerViewAllSongs.addItemDecoration(
                DividerItemDecoration(
                    recyclerViewAllSongs.context,
                    (recyclerViewAllSongs.layoutManager as LinearLayoutManager).orientation
                )
            )
        }
        return view
    }

}