package com.projects.musicplayer.fragments

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.projects.musicplayer.adapters.AllSongsAapter
import com.projects.musicplayer.R
import com.projects.musicplayer.adapters.PlaylistDialogAdapter
import com.projects.musicplayer.adapters.RecentTracksAdapter
import com.projects.musicplayer.database.PlaylistConverter
import com.projects.musicplayer.database.PlaylistEntity
import com.projects.musicplayer.database.RecentSongEntity
import com.projects.musicplayer.uicomponents.CustomDialog
import com.projects.musicplayer.viewmodel.*
import com.projects.musicplayer.database.SongEntity
import com.projects.musicplayer.rest.FavSongsViewModel
import com.projects.musicplayer.rest.FavSongsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Exception

class HomeFragment : Fragment() {

    lateinit var recyclerViewAllSongs: RecyclerView
    lateinit var adapterAllSongs: AllSongsAapter

    lateinit var recyclerViewRecentTracks: RecyclerView
    lateinit var adapterRecentTracks: RecentTracksAdapter

    lateinit var recentTrackBar: TextView
    lateinit var toolbar: androidx.appcompat.widget.Toolbar

    //view model related
    private lateinit var mAllSongsViewModel: AllSongsViewModel
    private lateinit var mAllSongsViewModelFactory: AllSongsViewModelFactory
    private lateinit var mRecentSongsViewModel: RecentSongsViewModel
    private lateinit var mRecentSongsViewModelFactory: RecentSongsViewModelFactory
    private lateinit var mFavSongsViewModel: FavSongsViewModel
    private lateinit var mFavSongsViewModelFactory: FavSongsViewModelFactory
       private lateinit var mPlaylistViewModel: PlaylistViewModel
    private lateinit var mPlaylistViewModelFactory: PlaylistViewModelFactory

    private lateinit var mMediaControlViewModel: MediaControlViewModel
    private lateinit var mMediaControlViewModelFactory: MediaControlViewModelFactory

    lateinit var createPlaylistDialog: AddToPlaylist
    lateinit var playlistInputDialog: CustomDialog

    private val uiscope = CoroutineScope(Dispatchers.Main)

    var selectedSongId = -1

    var onPlaySongClickCallback: ((song: SongEntity) -> Unit)? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**ViewModel for ALLSongs*/
        mAllSongsViewModelFactory = AllSongsViewModelFactory(activity!!.application)
        mAllSongsViewModel =
            ViewModelProvider(this, mAllSongsViewModelFactory).get(AllSongsViewModel::class.java)

        /**ViewModel for now playing songs**/
//        mMediaControlViewModelFactory = MediaControlViewModelFactory()
        mMediaControlViewModel =
            ViewModelProvider(activity!!).get(MediaControlViewModel::class.java)
//            ViewModelProvider(
//                this,
//                mMediaControlViewModelFactory
//            ).get(MediaControlViewModel::class.java)

        /**ViewModel for FavSongs*/
        mFavSongsViewModelFactory = FavSongsViewModelFactory(activity!!.application)
        mFavSongsViewModel =
            ViewModelProvider(this, mFavSongsViewModelFactory).get(FavSongsViewModel::class.java)


        mAllSongsViewModel.allSongs.observe(viewLifecycleOwner, Observer {
            Log.i("LIVEDATA-UPDATE", "Setting all songs again")
            adapterAllSongs.setSongs(it!!)
        })

        adapterAllSongs.favClickCallback = fun(id: Int) {
            //update fav whenever fav button clicked
            uiscope.launch {
                mAllSongsViewModel.updateFav(id)
            }
        }

        mPlaylistViewModelFactory = PlaylistViewModelFactory(activity!!.application)
        mPlaylistViewModel =
            ViewModelProvider(this, mPlaylistViewModelFactory).get(PlaylistViewModel::class.java)
        mPlaylistViewModel.allPlaylists.observe(viewLifecycleOwner, Observer {
            createPlaylistDialog.setDialogPlaylists(it!!)
        })

        /**ViewModel for RecentSongs*/
        mRecentSongsViewModelFactory = RecentSongsViewModelFactory(activity!!.application)
        mRecentSongsViewModel =
            ViewModelProvider(
                this,
                mRecentSongsViewModelFactory
            ).get(RecentSongsViewModel::class.java)

        mRecentSongsViewModel.recentSongs.observe(viewLifecycleOwner, Observer {
            Log.i("LIVEDATA-UPDATE", "Setting recent songs again")//TODO continue
            toolbar.visibility=View.GONE
            recentTrackBar.visibility=View.VISIBLE
            adapterRecentTracks.addTracks(it!!)
        })

        adapterAllSongs.onSongClickCallback = fun(recentSong: RecentSongEntity, song: SongEntity) {
            //update recent tracks
            uiscope.launch {
                //TODO both play song and add to recent
                mRecentSongsViewModel.insertAfterDeleteSong(recentSong)
                toolbar.visibility = View.GONE
                recentTrackBar.visibility = View.VISIBLE

                //TODO:LIVE DATA NOT OBSERVING IN MAINACTIVITY
                mMediaControlViewModel.nowPlayingSong.value = song
                onPlaySongClickCallback?.invoke(song)
                Log.d("NOWPLAYING-VIEWMODEL", "Now Playing from HOME FRAGMENT $song updated")

            }
//            if (song == mMediaControlViewModel.nowPlayingSong!!.value) {
//                mMediaControlViewModel.togglePlayPause()
//            }
//            else {

//            }
        }

        adapterRecentTracks.onSongClickCallback = fun(song: RecentSongEntity) {
            //update recent tracks
            uiscope.launch {
                //TODO both play song and add to recent
                mRecentSongsViewModel.insertAfterDeleteSong(song)
                //TODO play here using id
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        playlistInputDialog = CustomDialog(activity as Context)
        createPlaylistDialog = AddToPlaylist(activity as Context)
        recyclerViewAllSongs = view.findViewById(R.id.recyclerAllSongs)
        recyclerViewRecentTracks = view.findViewById(R.id.recyclerRecentTrack)
        recentTrackBar = view.findViewById(R.id.recentTrackBar)
        toolbar = view.findViewById(R.id.homeToolbar)
        /**ViewModel for playlists*/
        mPlaylistViewModelFactory = PlaylistViewModelFactory(activity!!.application)
        mPlaylistViewModel =
            ViewModelProvider(
                this,
                mPlaylistViewModelFactory
            ).get(PlaylistViewModel::class.java)




        createPlaylistDialog.setOnDismissListener {
            val playlistId: Int = createPlaylistDialog.selectedPlaylistId
            if (playlistId != -1) {
                var songs: String? = "sample"
              /*  mPlaylistViewModel.allPlaylists.observe(viewLifecycleOwner,Observer {
                    mPlaylistViewModel.getPlaylistSongsById(playlistId).observe(viewLifecycleOwner,Observer{
                        songs= it
                    })
                })*/

                //TODO: ADD selectedSongId to playlistId
                runBlocking{
                        songs = mPlaylistViewModel.getPlaylistSongsById(playlistId)

                }
                uiscope.launch {
                    val listOfSongs : List<Int>? = PlaylistConverter.toList(songs)
                    if(listOfSongs==null)
                        mPlaylistViewModel.updatePlaylist(playlistId,listOf(selectedSongId))
                    else{
                        val songs = (listOfSongs as MutableList<Int>)
                        if(!songs.remove(selectedSongId)){
                            Toast.makeText(
                                activity as Context,
                                "$selectedSongId added to $playlistId",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else{
                            Toast.makeText(
                                activity as Context,
                                "$selectedSongId already in $playlistId",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        songs.add(selectedSongId)
                        Log.i("PLAYLISTSONGS", songs.toString())
                        mPlaylistViewModel.updatePlaylist(playlistId,songs)
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

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        playlistInputDialog = CustomDialog(activity as Context)
        createPlaylistDialog = AddToPlaylist(activity as Context)
        recyclerViewAllSongs = view.findViewById(R.id.recyclerAllSongs)
        recyclerViewRecentTracks = view.findViewById(R.id.recyclerRecentTrack)
        recentTrackBar=view.findViewById(R.id.recentTrackBar)
        toolbar=view.findViewById(R.id.homeToolbar)



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
                    createPlaylistDialog.show()
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

class AddToPlaylist(
    context: Context
) : Dialog(context){

    private var playlists: List<PlaylistEntity>? = null

    fun setDialogPlaylists(_playlists: List<PlaylistEntity>) {
        playlists = _playlists
    }


    var recyclerView: RecyclerView? = null
    var adapter: PlaylistDialogAdapter? = null

    var selectedPlaylistId = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_to_playlist_dialog)


        recyclerView = findViewById(R.id.recyclerViewPlaylists)

        adapter = PlaylistDialogAdapter(context)
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = LinearLayoutManager(context)

        adapter?.playlistClickCallback = fun(id: Int) {
            selectedPlaylistId = id
            dismiss()
        }

        adapter?.setPlayLists(playlists!!)
    }
}

