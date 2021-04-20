package com.projects.musicplayer

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class Playlists : Fragment() {
    
    lateinit var recyclerViewPlaylists: RecyclerView
    lateinit var recylcerViewPlaylistadapter:PlaylistAdapter
    lateinit var toolbar: Toolbar


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
         val view = inflater.inflate(R.layout.fragment_playlists, container, false)
        recyclerViewPlaylists=view.findViewById(R.id.recyclerViewPlaylists)
        toolbar = view.findViewById(R.id.toolbar)
        toolbar.title="Playlists"

        if (activity != null) {

            val playlists=listOf(
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
                PlaylistModel("Playlist 15")
            )

            recylcerViewPlaylistadapter = PlaylistAdapter(activity as Context,playlists)
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