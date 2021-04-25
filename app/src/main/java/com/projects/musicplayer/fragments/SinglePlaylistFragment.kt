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
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.projects.musicplayer.R
import com.projects.musicplayer.adapters.SinglePlaylistAdapter
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
import java.lang.Exception


class SinglePlaylistFragment : Fragment() {
    lateinit var toolbar:Toolbar
    lateinit var singlePlaylistRecyclerView:RecyclerView
    lateinit var singlePlaylistRecyclerViewAdapter: SinglePlaylistAdapter

    //view model related //TODO Check
    private lateinit var mRecentSongsViewModel: RecentSongsViewModel
    private lateinit var mRecentSongsViewModelFactory: RecentSongsViewModelFactory
    private lateinit var mPlaylistViewModel: PlaylistViewModel
    private lateinit var mPlaylistViewModelFactory: PlaylistViewModelFactory
    private lateinit var mAllSongsViewModel: AllSongsViewModel
    private lateinit var mAllSongsViewModelFactory: AllSongsViewModelFactory
    private lateinit var mFavSongsViewModel: FavSongsViewModel
    private lateinit var mFavSongsViewModelFactory: FavSongsViewModelFactory
    private lateinit var mMediaControlViewModel: MediaControlViewModel

    private val uiscope = CoroutineScope(Dispatchers.Main)

    //playlist info
    //TODO for obtaining info for this playlist
    private var playlistId=0
    private var playlistName="Playlist"
    private var playListSongs = "songs "

    var selectedSongId=-1

    //TODO ViewModel for single playlist
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** Viewmodel for ALLSongs*/
        mAllSongsViewModelFactory = AllSongsViewModelFactory(activity!!.application)
        mAllSongsViewModel =
            ViewModelProvider(this, mAllSongsViewModelFactory).get(AllSongsViewModel::class.java)

        /** Viewmodel for Playlist*/
        mPlaylistViewModelFactory = PlaylistViewModelFactory(activity!!.application)
        mPlaylistViewModel =
            ViewModelProvider(this, mPlaylistViewModelFactory).get(PlaylistViewModel::class.java)

        mPlaylistViewModel.allPlaylists.observe(viewLifecycleOwner, Observer {
            mPlaylistViewModel.getPlaylistSongsByIdLive(playlistId).observe(viewLifecycleOwner,Observer{
                var mSongs = mutableListOf<SongEntity>()
                runBlocking {
                    val listSongIds=PlaylistConverter.toList(it)
                    if(listSongIds!=null){
                        for (id in listSongIds){
                            mSongs.add(mAllSongsViewModel.getSongByIdSuspend(id))
                        }
                    }
                    else{
                        //TODO print no songs, add some
                    }
                }
                Log.i("LIVEDATAPLAYLISTUPDATE",mSongs.toString())
                singlePlaylistRecyclerViewAdapter.setSongs(mSongs)
            })

        })

        /**ViewModel for FavSongs*/
        mFavSongsViewModelFactory =
            FavSongsViewModelFactory(activity!!.application)
        mFavSongsViewModel =
            ViewModelProvider(this, mFavSongsViewModelFactory).get(FavSongsViewModel::class.java)

        singlePlaylistRecyclerViewAdapter.favClickCallback = fun(id: Int) {
            //update fav whenever fav button clicked
            uiscope.launch {
                //TODO add to favourites both places
                mAllSongsViewModel.updateFav(id)
                //TODO in fav database also
                /*Log.e("FAV",mAllSongsViewModel.checkFav(id).toString());
                if(mAllSongsViewModel.checkFav(id)==1)
                    mFavSongsViewModel.insertSong(FavEntity(id))
                else
                    mFavSongsViewModel.removeSong(FavEntity(id))*/
            }
        }

        /** Viewmodel for RecentSongs*/
        mRecentSongsViewModelFactory = RecentSongsViewModelFactory(activity!!.application)
        mRecentSongsViewModel = ViewModelProvider(this, mRecentSongsViewModelFactory).get(RecentSongsViewModel::class.java)

        /** Viewmodel for MediaControl*/
        mMediaControlViewModel = ViewModelProvider(activity!!).get(MediaControlViewModel::class.java)


            singlePlaylistRecyclerViewAdapter.onSongClickCallback = fun(recentSong: RecentSongEntity,song:SongEntity,allSongs:List<SongEntity>) {
                //update fav whenever fav button clicked
                uiscope.launch {
                    //TODO both play song and add to recent
                    mRecentSongsViewModel.insertAfterDeleteSong(recentSong)
                    mMediaControlViewModel.nowPlayingSong.value = song
                    mMediaControlViewModel.nowPlayingSongs.value=allSongs
                    mMediaControlViewModel.nowPlaylist.value = playlistName
                    Log.d("NOWPLAYING-VIEWMODEL", "Now Playing from HOME FRAGMENT $song updated")

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


        if (activity != null){
            // set this playlist according to which fragment called it
            playlistId = arguments?.get("ID") as Int
            playlistName = arguments?.get("NAME") as String
            //TODO Only for debugging purposes, otherwise this argument will be deleted from Bundle
            playListSongs = arguments?.get("SONGS") as String
            Log.i("PLAYLISTINFO",playlistName)
            Log.i("PLAYLISSONGTINFO",playListSongs.length.toString())

            toolbar.title = playlistName

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

    override fun onContextItemSelected(item: MenuItem): Boolean {

        try {
            selectedSongId = singlePlaylistRecyclerViewAdapter.getSelectedSongId()
            Log.e("REMOVESONG", selectedSongId.toString())
        } catch (e: Exception) {
            Log.e("REMOVESONG",e.message.toString())
            return super.onContextItemSelected(item)
        }
        when (item.itemId) {
            R.id.ctx_remove_from_playlist -> {
                var songs:String? = "Sample"
                runBlocking {
                    songs = mPlaylistViewModel.getPlaylistSongsById(playlistId)
                }
                uiscope.launch {
                    val listOfSongs : List<Int>? = PlaylistConverter.toList(songs)
                    if(listOfSongs==null)
                        Log.e("NOSONG","No song to delete which is not possible")
                    else{
                        val mutableSongs = (listOfSongs as MutableList<Int>)
                        mutableSongs.remove(selectedSongId)
                        Log.i("PLAYLISTSONGS", songs.toString())
                        mPlaylistViewModel.updatePlaylist(playlistId,mutableSongs)
                    }
                }
            }
            else -> {
                Toast.makeText(activity as Context,"No Playlist Selected", Toast.LENGTH_SHORT).show()
            }
        }

        return super.onContextItemSelected(item)
    }
}