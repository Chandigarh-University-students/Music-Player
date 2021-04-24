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
import com.projects.musicplayer.database.PlaylistConverter
import com.projects.musicplayer.database.RecentSongEntity
import com.projects.musicplayer.database.SongEntity
import com.projects.musicplayer.viewmodel.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


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

    private val uiscope = CoroutineScope(Dispatchers.Main)

    //playlist info
    //TODO for obtaining info for this playlist
    private var playlistId=0
    private var playlistName="Playlist"
    private var playListSongs = "songs "


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
            Log.i("LIVEDATAPLAYLISTUPDATE","Setting all songs again in playlist")
                uiscope.launch {
                val songIDs = PlaylistConverter.toList(mPlaylistViewModel.getPlaylistSongsById(playlistId).value)
                val songList : MutableList<SongEntity> = mutableListOf<SongEntity>()
                if (songIDs != null) {
                    for(id in songIDs) {
                        songList.add(mAllSongsViewModel.getSongById(id))
                    }
                    Log.i("SONGID"," List<Int> songIds "+songIDs.size.toString())
                    Log.i("SONGLIST"," List<SongEntity> songs "+songList.size.toString())
                    singlePlaylistRecyclerViewAdapter.setSongs(songList)
                }
                else {
                    Log.e("ERRORPLAYLIST","No songs")

                }
            }
        })

        /**ViewModel for FavSongs*/
        mFavSongsViewModelFactory = FavSongsViewModelFactory(activity!!.application)
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
            mRecentSongsViewModel =
                ViewModelProvider(this, mRecentSongsViewModelFactory).get(RecentSongsViewModel::class.java)


            singlePlaylistRecyclerViewAdapter.onSongClickCallback = fun(recentSong: RecentSongEntity,song:SongEntity) {
                //update fav whenever fav button clicked
                uiscope.launch {
                    //TODO both play song and add to recent
                    mRecentSongsViewModel.insertAfterDeleteSong(recentSong)

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
}