package com.projects.musicplayer.fragments

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.projects.musicplayer.adapters.PlaylistAdapter
import com.projects.musicplayer.rest.PlaylistModel
import com.projects.musicplayer.R
import com.projects.musicplayer.database.PlaylistEntity
import com.projects.musicplayer.uicomponents.CustomDialog
import com.projects.musicplayer.viewmodel.*
import kotlinx.android.synthetic.main.playlist_dialog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class PlaylistsFragment : Fragment() {

    lateinit var recyclerViewPlaylists: RecyclerView
    lateinit var recylcerViewPlaylistadapter: PlaylistAdapter
    lateinit var toolbar: Toolbar
    lateinit var fabCreatePlaylist: FloatingActionButton
    lateinit var playlistInputDialog: CustomDialog

    //view model related
    private lateinit var mPlaylistViewModel: PlaylistViewModel
    private lateinit var mPlaylistViewModelFactory: PlaylistViewModelFactory

//    private lateinit var mRecentSongsViewModel: RecentSongsViewModel
//    private lateinit var mRecentSongsViewModelFactory: RecentSongsViewModelFactory

    private val uiscope = CoroutineScope(Dispatchers.Main)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPlaylistViewModelFactory = PlaylistViewModelFactory(activity!!.application)
        mPlaylistViewModel =
            ViewModelProvider(this, mPlaylistViewModelFactory).get(PlaylistViewModel::class.java)

        mPlaylistViewModel.allPlaylists.observe(viewLifecycleOwner, Observer {
            recylcerViewPlaylistadapter.setPlayLists(it!!)
        })

        recylcerViewPlaylistadapter.onPlaylistClickCallback = fun (playlist:PlaylistEntity) {
            //load singlePlaylist fragment into frame layout...
            // always open SInglePlaylist with a Bundle except when adding a song to playlist
            val bundle=Bundle()
            bundle.putInt("ID",playlist.id)
            bundle.putString("NAME",playlist.name)
            bundle.putString("SONGS",playlist.songs)
            activity!!.supportFragmentManager.beginTransaction()
                .add(R.id.frame,SinglePlaylistFragment::class.java,bundle)
                .commit()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_playlists, container, false)
        recyclerViewPlaylists = view.findViewById(R.id.recyclerViewPlaylists)
        toolbar = view.findViewById(R.id.toolbar)
        fabCreatePlaylist = view.findViewById(R.id.fabCreatePlaylist)
        playlistInputDialog = CustomDialog(activity as Context)
        toolbar.title = "PlaylistsFragment"

        fabCreatePlaylist.setOnClickListener {
            playlistInputDialog.show()
        }

        playlistInputDialog.positiveButtonCallback = fun(playlistName: String) {
            if (playlistName.isNotBlank()) {
                mPlaylistViewModel.createPlaylist(
                    PlaylistEntity(
                        playlistName.hashCode(),
                        playlistName,
                        ""
                    )
                )
                Toast.makeText(
                    activity as Context,
                    "$playlistName playlist created ",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    activity as Context,
                    "Discarding Empty Playlist Name",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (activity != null) {

            recylcerViewPlaylistadapter =
                PlaylistAdapter(
                    activity as Context
                )
            recyclerViewPlaylists.adapter = recylcerViewPlaylistadapter
            recyclerViewPlaylists.layoutManager = LinearLayoutManager(activity)
            recyclerViewPlaylists.addItemDecoration(
                DividerItemDecoration(
                    recyclerViewPlaylists.context,
                    (recyclerViewPlaylists.layoutManager as LinearLayoutManager).orientation
                )
            )

        }

        return view
    }


}

