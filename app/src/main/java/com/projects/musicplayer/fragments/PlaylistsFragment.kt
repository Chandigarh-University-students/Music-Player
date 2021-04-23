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
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.projects.musicplayer.adapters.PlaylistAdapter
import com.projects.musicplayer.rest.PlaylistModel
import com.projects.musicplayer.R
import com.projects.musicplayer.uicomponents.CustomDialog
import kotlinx.android.synthetic.main.playlist_dialog.*


class PlaylistsFragment : Fragment() {

    lateinit var recyclerViewPlaylists: RecyclerView
    lateinit var recylcerViewPlaylistadapter: PlaylistAdapter
    lateinit var toolbar: Toolbar
    lateinit var fabCreatePlaylist: FloatingActionButton
    lateinit var playlistInputDialog: CustomDialog


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
                //TODO: CREATE NEW PLAYLIST
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

            val playlists = listOf(
                PlaylistModel("Playlist 1"),
                PlaylistModel("Playlist 2"),
                PlaylistModel("Playlist 3"),
                PlaylistModel("Playlist 4"),
                PlaylistModel("Playlist 5"),
                PlaylistModel("Playlist 6"),
                PlaylistModel("Playlist 7"),
                PlaylistModel("Playlist 8"),
                PlaylistModel("Playlist 9"),
                PlaylistModel("Playlist 10"),
                PlaylistModel("Playlist 11"),
                PlaylistModel("Playlist 12"),
                PlaylistModel("Playlist 13"),
                PlaylistModel("Playlist 14"),
                PlaylistModel("Playlist 15"),
                PlaylistModel("Playlist 16"),
                PlaylistModel("Playlist 17"),
                PlaylistModel("Playlist 18"),
                PlaylistModel("Playlist 19"),
                PlaylistModel("Playlist 20"),
                PlaylistModel("Playlist 21"),
                PlaylistModel("Playlist 22"),
                PlaylistModel("Playlist 23"),
                PlaylistModel("Playlist 24")

            )

            recylcerViewPlaylistadapter =
                PlaylistAdapter(
                    activity as Context,
                    playlists
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

