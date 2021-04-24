package com.projects.musicplayer.fragments

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import com.projects.musicplayer.R
import com.projects.musicplayer.adapters.PlaylistAdapter
import com.projects.musicplayer.database.PlaylistEntity
import com.projects.musicplayer.database.RecentSongEntity
import com.projects.musicplayer.rest.PlaylistModel
import com.projects.musicplayer.viewmodel.PlaylistViewModel
import com.projects.musicplayer.viewmodel.PlaylistViewModelFactory


class AddToPlaylistDialogFragment : BottomSheetDialogFragment() {

    //view model related
    private lateinit var mPlaylistViewModel: PlaylistViewModel
    private lateinit var mPlaylistViewModelFactory: PlaylistViewModelFactory

    lateinit var recyclerViewPlaylists: RecyclerView
//    lateinit var recylcerViewPlaylistadapter: PlaylistEntityAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(
            R.layout.fragment_add_to_playlist_dialog_list_dialog,
            container,
            false
        )
        recyclerViewPlaylists = view.findViewById(R.id.recyclerViewPlaylists)

        if (activity != null) {


//            recylcerViewPlaylistadapter =
//                PlaylistEntityAdapter(
//                    activity as Context
//                )
//            recyclerViewPlaylists.adapter = recylcerViewPlaylistadapter
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


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        mPlaylistViewModelFactory = PlaylistViewModelFactory(activity!!.application)
//        mPlaylistViewModel =
//            ViewModelProvider(this, mPlaylistViewModelFactory).get(PlaylistViewModel::class.java)
//
//        recyclerViewPlaylists = activity?.findViewById(R.id.list)!!
//        activity?.findViewById<RecyclerView>(R.id.list)?.layoutManager =
//            LinearLayoutManager(context)
//
//        mPlaylistViewModel.allPlaylists.observe(viewLifecycleOwner, Observer {
//            recylcerViewPlaylistadapter.setPlayLists(it!!)
//        })
//
//    }

    /*
    private inner class ViewHolder internal constructor(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) : RecyclerView.ViewHolder(
        inflater.inflate(
            R.layout.single_playlist_item,
            parent,
            false
        )
    ) {
        internal val playlistName: TextView = itemView.findViewById(R.id.playlistName)
        internal val playlistCard: CardView = itemView.findViewById(R.id.FavoritesCardView)

    }
    */
    private inner class ViewHolder internal constructor(
        view: View
    ) : RecyclerView.ViewHolder(view) {
        val playlistName: TextView = itemView.findViewById(R.id.playlistName)
        val playlistCard: CardView = itemView.findViewById(R.id.FavoritesCardView)

    }

    private inner class PlaylistEntityAdapter(context: Context) :
        RecyclerView.Adapter<ViewHolder>() {

        var onPlaylistClickCallback: ((playlistId: Int) -> Unit)? = null

        val mInflater: LayoutInflater = LayoutInflater.from(context)

        private var playlists: List<PlaylistEntity>? = null


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val playlistItemView: View =
                mInflater.inflate(R.layout.single_playlist_item, parent, false)
            return ViewHolder(playlistItemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val currentPlaylist: PlaylistEntity = playlists!![position]
            holder.playlistName.text = currentPlaylist.name
            holder.playlistName.text = position.toString()
            holder.playlistName.text = currentPlaylist.name

            holder.playlistCard.setOnClickListener {
                onPlaylistClickCallback?.invoke(currentPlaylist.id)
            }

        }

        fun setPlayLists(mplaylists: List<PlaylistEntity>) {
            playlists = mplaylists
            notifyDataSetChanged()
        }

        override fun getItemCount(): Int {
            return if (playlists != null)
                playlists!!.size;
            else 0;
        }
    }


}