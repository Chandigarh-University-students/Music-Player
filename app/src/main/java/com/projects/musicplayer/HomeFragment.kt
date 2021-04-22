package com.projects.musicplayer

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
import com.projects.musicplayer.viewmodel.AllSongsViewModel
import com.projects.musicplayer.viewmodel.AllSongsViewModelFactory
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

    private val uiscope = CoroutineScope(Dispatchers.Main)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAllSongsViewModelFactory = AllSongsViewModelFactory(activity!!.application)
        mAllSongsViewModel =
            ViewModelProvider(this, mAllSongsViewModelFactory).get(AllSongsViewModel::class.java)

        mAllSongsViewModel.allSongs.observe(viewLifecycleOwner, Observer {
            Log.i("LIVEDATA-UPDATE","Setting all songs again")
            adapterAllSongs.setSongs(it!!)
        })

        adapterAllSongs.favClickCallback = fun(id: Int) {
            //update fav whenever fav button clicked
            uiscope.launch {
                mAllSongsViewModel.updateFav(id)
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


//            adapterAllSongs.setSongs(
//                listOf(
//                    Song("In Motion", "Trent Renzor and Atticus Ross"),
//                    Song("In Motion", "Trent Renzor and Atticus Ross"),
//                    Song("In Motion", "Trent Renzor and Atticus Ross"),
//                    Song("In Motion", "Trent Renzor and Atticus Ross"),
//                    Song("In Motion", "Trent Renzor and Atticus Ross"),
//                    Song("In Motion", "Trent Renzor and Atticus Ross"),
//                    Song("In Motion", "Trent Renzor and Atticus Ross"),
//                    Song("In Motion", "Trent Renzor and Atticus Ross"),
//                    Song("In Motion", "Trent Renzor and Atticus Ross"),
//                    Song("In Motion", "Trent Renzor and Atticus Ross"),
//                    Song("In Motion", "Trent Renzor and Atticus Ross")
//                )
//            )
            adapterRecentTracks.setTotalTracks(10)
        }

        return view
    }

}