package com.projects.musicplayer

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    lateinit var recyclerViewAllSongs: RecyclerView
    lateinit var adapterAllSongs: AllSongsAapter

    lateinit var recyclerViewRecentTracks: RecyclerView
    lateinit var adapterRecentTracks: RecentTracksAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerViewAllSongs = view.findViewById(R.id.recyclerAllSongs)
        recyclerViewRecentTracks = view.findViewById(R.id.recyclerRecentTrack)


        if (activity != null) {
            adapterAllSongs = AllSongsAapter(activity as Context)
            adapterRecentTracks = RecentTracksAdapter(activity as Context)

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

            adapterAllSongs.setSongs(
                listOf(
                    Song("In Motion", "Trent Renzor and Atticus Ross"),
                    Song("In Motion", "Trent Renzor and Atticus Ross"),
                    Song("In Motion", "Trent Renzor and Atticus Ross"),
                    Song("In Motion", "Trent Renzor and Atticus Ross"),
                    Song("In Motion", "Trent Renzor and Atticus Ross"),
                    Song("In Motion", "Trent Renzor and Atticus Ross"),
                    Song("In Motion", "Trent Renzor and Atticus Ross"),
                    Song("In Motion", "Trent Renzor and Atticus Ross"),
                    Song("In Motion", "Trent Renzor and Atticus Ross"),
                    Song("In Motion", "Trent Renzor and Atticus Ross"),
                    Song("In Motion", "Trent Renzor and Atticus Ross")
                )
            )
            adapterRecentTracks.setTotalTracks(10)
        }

        return view
    }

}