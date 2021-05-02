package com.projects.musicplayer.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.projects.musicplayer.adapters.AllSongsAdapter
import com.projects.musicplayer.R
import com.projects.musicplayer.adapters.RecentTracksAdapter
import com.projects.musicplayer.database.playlists.PlaylistConverter
import com.projects.musicplayer.database.recentSongs.RecentSongEntity
import com.projects.musicplayer.uicomponents.CustomDialog
import com.projects.musicplayer.database.allSongs.SongEntity
import com.projects.musicplayer.uicomponents.AddToPlaylist
import com.projects.musicplayer.uicomponents.BounceEdgeEffectFactory
import com.projects.musicplayer.utils.Utility
import com.projects.musicplayer.viewmodel.allSongs.AllSongsViewModel
import com.projects.musicplayer.viewmodel.allSongs.AllSongsViewModelFactory
import com.projects.musicplayer.viewmodel.mediaControl.MediaControlViewModel
import com.projects.musicplayer.viewmodel.playlists.PlaylistViewModel
import com.projects.musicplayer.viewmodel.playlists.PlaylistViewModelFactory
import com.projects.musicplayer.viewmodel.recentSongs.RecentSongsViewModel
import com.projects.musicplayer.viewmodel.recentSongs.RecentSongsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.util.*

class HomeFragment : Fragment() {

    lateinit var recyclerViewAllSongs: RecyclerView
    lateinit var adapterAllSongs: AllSongsAdapter

    lateinit var recyclerViewRecentTracks: RecyclerView
    lateinit var adapterRecentTracks: RecentTracksAdapter

    lateinit var toolbar: androidx.appcompat.widget.Toolbar

    //view model related
    private lateinit var mAllSongsViewModel: AllSongsViewModel
    private lateinit var mAllSongsViewModelFactory: AllSongsViewModelFactory
    private lateinit var mRecentSongsViewModel: RecentSongsViewModel
    private lateinit var mRecentSongsViewModelFactory: RecentSongsViewModelFactory

    private lateinit var mPlaylistViewModel: PlaylistViewModel
    private lateinit var mPlaylistViewModelFactory: PlaylistViewModelFactory

    private lateinit var mMediaControlViewModel: MediaControlViewModel

    lateinit var addToPlaylistDialog: AddToPlaylist
    lateinit var playlistInputDialog: CustomDialog

    private val uiscope = CoroutineScope(Dispatchers.Main)

    var selectedSongId = -1
    var hasCreatedPlaylist = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**ViewModel for ALLSongs*/
        mAllSongsViewModelFactory =
            AllSongsViewModelFactory(
                activity!!.application
            )
        mAllSongsViewModel =
            ViewModelProvider(this, mAllSongsViewModelFactory).get(AllSongsViewModel::class.java)

        /**ViewModel for now playing songs**/
        mMediaControlViewModel =
            ViewModelProvider(activity!!).get(MediaControlViewModel::class.java)


        mAllSongsViewModel.allSongs.observe(viewLifecycleOwner, Observer {
            Log.i("LIVEDATA-UPDATE", "Setting all songs again")
            if(it.isNullOrEmpty())
                adapterAllSongs.setSongs(it!!)
            else{
                val tempSongList = mutableListOf<SongEntity>()
                tempSongList.addAll(it)
                Collections.sort(tempSongList,Utility.songComparator)
                adapterAllSongs.setSongs(tempSongList)
            }


            //set up data for first time
            if (!it.isNullOrEmpty() && mMediaControlViewModel.nowPlayingSong.value == null) {
                mMediaControlViewModel.nowPlayingSong.value = it.random()
                mMediaControlViewModel.nowPlayingSongs.value = it
            }
        })

        adapterAllSongs.favClickCallback = fun(id: Int) {
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

        mPlaylistViewModelFactory =
            PlaylistViewModelFactory(
                activity!!.application
            )
        mPlaylistViewModel =
            ViewModelProvider(this, mPlaylistViewModelFactory).get(PlaylistViewModel::class.java)
        mPlaylistViewModel.allPlaylists.observe(viewLifecycleOwner, Observer {
            hasCreatedPlaylist = !it.isNullOrEmpty()
            addToPlaylistDialog.setDialogPlaylists(it!!)
        })

        /**ViewModel for RecentSongs*/
        mRecentSongsViewModelFactory =
            RecentSongsViewModelFactory(
                activity!!.application
            )
        mRecentSongsViewModel =
            ViewModelProvider(
                this,
                mRecentSongsViewModelFactory
            ).get(RecentSongsViewModel::class.java)

        mRecentSongsViewModel.recentSongs.observe(viewLifecycleOwner, Observer {
            Log.i("LIVEDATA-UPDATE", "Setting recent songs again")
            if(!it.isEmpty())
                toolbar.title = "Recent Tracks"
            else
                toolbar.title = "Home"
            adapterRecentTracks.addTracks(it!!)
        })

        adapterAllSongs.onSongClickCallback =
            fun(song: SongEntity, allSongs: List<SongEntity>) {
                //update recent tracks
                uiscope.launch {
                    //mRecentSongsViewModel.insertAfterDeleteSong(recentSong)
                    //LIVE DATA NOT OBSERVING IN MAINACTIVITY
                    mMediaControlViewModel.nowPlayingSong.value = song
                    mMediaControlViewModel.nowPlayingSongs.value = allSongs
                    mMediaControlViewModel.nowPlaylist.value = "All Songs"
                }
            }


        adapterRecentTracks.onSongClickCallback = fun(song: RecentSongEntity) {
            //update recent tracks
            var songPlayed: SongEntity
            var allSongs: List<SongEntity>
            runBlocking {
                songPlayed = mAllSongsViewModel.getSongByIdSuspend(song.songId)
                allSongs = mAllSongsViewModel.getAllSongs()
            }
            uiscope.launch {
                mMediaControlViewModel.nowPlaylist.value = "Recent Tracks"
                mMediaControlViewModel.nowPlayingSong.value = songPlayed
                mMediaControlViewModel.nowPlayingSongs.value = allSongs
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        playlistInputDialog = CustomDialog(activity as Context)
        addToPlaylistDialog = AddToPlaylist(activity as Context)
        recyclerViewAllSongs = view.findViewById(R.id.recyclerAllSongs)
        recyclerViewRecentTracks = view.findViewById(R.id.recyclerRecentTrack)
        toolbar = view.findViewById(R.id.homeToolbar)
        /**ViewModel for playlists*/
        mPlaylistViewModelFactory =
            PlaylistViewModelFactory(
                activity!!.application
            )
        mPlaylistViewModel =
            ViewModelProvider(
                this,
                mPlaylistViewModelFactory
            ).get(PlaylistViewModel::class.java)




        addToPlaylistDialog.setOnDismissListener {
            val playlistId: Int = addToPlaylistDialog.selectedPlaylistId
            Log.i("PLAYLISTSONGS", "Dismiss Listener called with playlistId=$playlistId")
            if (playlistId != -1) {
                var songs: String? = "sample"

                runBlocking {
                    songs = mPlaylistViewModel.getPlaylistSongsById(playlistId)
                }
                uiscope.launch {
                    val listOfSongs: List<Int>? = PlaylistConverter.toList(songs)
                    if (listOfSongs == null) {
                        Toast.makeText(
                            activity as Context,
                            "Added to Playlist",
                            Toast.LENGTH_SHORT
                        ).show()
                        mPlaylistViewModel.updatePlaylist(playlistId, listOf(selectedSongId))
                    }
                    else {
                        val songsList = (listOfSongs as MutableList<Int>)
                        if (!songsList.contains(selectedSongId)) {
                            songsList.add(selectedSongId)
                            Toast.makeText(
                                activity as Context,
                                "Added to Playlist",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.i("PLAYLISTSONGS", songsList.toString())
                            mPlaylistViewModel.updatePlaylist(playlistId, songsList)
                        } else {
                            Toast.makeText(
                                activity as Context,
                                "Already in Playlist",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

            } else {
                Toast.makeText(
                    activity as Context,
                    "No Playlist Selected",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        if (activity != null) {

            adapterAllSongs =
                AllSongsAdapter(activity as Context)
            adapterRecentTracks =
                RecentTracksAdapter(activity as Context)

            recyclerViewAllSongs.adapter = adapterAllSongs

            recyclerViewAllSongs.edgeEffectFactory = BounceEdgeEffectFactory()

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

    override fun onContextItemSelected(item: MenuItem): Boolean {

        try {
            selectedSongId = adapterAllSongs.getSelectedSongId()
        } catch (e: Exception) {
            e.printStackTrace()
            return super.onContextItemSelected(item)
        }
        when (item.itemId) {
            R.id.ctx_add_to_playlist -> {

                try {
                    if(hasCreatedPlaylist)
                        addToPlaylistDialog.show()
                    else
                        Toast.makeText(activity as Context,"Create a playlist first",Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            else -> {

            }
        }

        return super.onContextItemSelected(item)
    }
}




