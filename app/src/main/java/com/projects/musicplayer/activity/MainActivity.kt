package com.projects.musicplayer.activity

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.projects.musicplayer.fragments.HomeFragment
import com.projects.musicplayer.fragments.PlaylistsFragment
import com.projects.musicplayer.R
import com.projects.musicplayer.database.SongEntity
import com.projects.musicplayer.rest.Song
import com.projects.musicplayer.fragments.FavFragment
import com.projects.musicplayer.fragments.SinglePlaylistFragment
import com.projects.musicplayer.uicomponents.RepeatTriStateButton
import com.projects.musicplayer.viewmodel.AllSongsViewModel
import com.projects.musicplayer.viewmodel.AllSongsViewModelFactory
import com.projects.musicplayer.viewmodel.MediaControlViewModel
import com.projects.musicplayer.viewmodel.MediaControlViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Long.parseLong
import java.time.Duration
import java.util.Arrays.equals
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    lateinit var bottomNavigationView: BottomNavigationView
    lateinit var mBottomSheetBehavior: BottomSheetBehavior<LinearLayoutCompat>
    lateinit var flFragment: FrameLayout
    lateinit var bottomSheet: LinearLayoutCompat

    lateinit var b_sheet_Collapsed: LinearLayout
    lateinit var b_sheet_Expanded: RelativeLayout
    lateinit var sharedPreferences: SharedPreferences


    //testing
//    lateinit var testState: TextView
//    lateinit var musicCoverPic:ImageView
    /**Now Playing Controls*/

    /*EXPANDED BOTTOM SHEET ELEMENTS*/
    //toolbar elements
    lateinit var btnMinimizeToolbar: ImageButton
    lateinit var txtCurrPlaying: TextView
    lateinit var btnSongList: ImageButton

    //current song in now playing
    lateinit var songNowPlaying: CardView
    lateinit var txtSongName: TextView
    lateinit var txtSongArtistName: TextView
    lateinit var btnFav: ToggleButton
    //current song in now playing

    lateinit var controlSeekBar: SeekBar
    lateinit var txtCurrentDuration: TextView
    lateinit var txtTotalDuration: TextView
    lateinit var btnControlShuffle: ToggleButton
    lateinit var btnRepeatControl: RepeatTriStateButton

    lateinit var btnPrevControl: ImageButton
    lateinit var btnPlayPauseControl: ToggleButton
    lateinit var btnNextControl: ImageButton


    private val READ_STORAGE_PERMISSION_REQUEST_CODE = 1
    private val TAG = "PermissionDemo"


    //view model related
    private lateinit var mAllSongsViewModel: AllSongsViewModel
    private lateinit var mAllSongsViewModelFactory: AllSongsViewModelFactory

    private lateinit var mMediaControlViewModel: MediaControlViewModel
    private lateinit var mMediaControlViewModelFactory: MediaControlViewModelFactory

    lateinit var mediaPlayer: MediaPlayer
    lateinit var runnable: Runnable

    val MediaPlayer.seconds: Int
        get() = this.duration / 1000
    val MediaPlayer.currentSeconds: Int
        get() = this.currentPosition / 1000
    var homeFragment: HomeFragment = HomeFragment()
    var handler: Handler = Handler()


    //coroutine scopes
    val uiscope = CoroutineScope(Dispatchers.Main)
    val dbScope = CoroutineScope(Dispatchers.IO)

    /*EXPANDED BOTTOM SHEET ELEMENTS*/


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomSheet = findViewById(R.id.bottom_sheet)
        flFragment = findViewById(R.id.frame)
        b_sheet_Collapsed = findViewById(R.id.b_sheet_Collapsed)
        b_sheet_Expanded = findViewById(R.id.b_sheet_Expanded)




        btnMinimizeToolbar = findViewById(R.id.btnMinimizeToolbar)
        txtCurrPlaying = findViewById(R.id.txtCurrPlaying)
        btnSongList = findViewById(R.id.btnSongList)

        songNowPlaying = findViewById(R.id.songNowPlaying)
        txtSongName = findViewById(R.id.txtSongName)
        txtSongArtistName = findViewById(R.id.txtSongArtistName)
        btnFav = findViewById(R.id.btnFav)
        controlSeekBar = findViewById(R.id.controlSeekBar)
        txtCurrentDuration = findViewById(R.id.txtCurrentDuration)
        txtTotalDuration = findViewById(R.id.txtTotalDuration)
        btnControlShuffle = findViewById(R.id.btnControlShuffle)
        btnRepeatControl = findViewById(R.id.btnRepeatControl)

        btnPrevControl = findViewById(R.id.btnPrevControl)
        btnPlayPauseControl = findViewById(R.id.btnPlayPauseControl)
        btnNextControl = findViewById(R.id.btnNextControl)


        sharedPreferences = getSharedPreferences(
            "Audio DB Preferences",
            Context.MODE_PRIVATE
        )

        //init View Model obejct
        //use of view model factory to pass parameter to view model
        mAllSongsViewModelFactory = AllSongsViewModelFactory(application)
        mAllSongsViewModel =
            ViewModelProvider(this, mAllSongsViewModelFactory).get(AllSongsViewModel::class.java)
        mMediaControlViewModel =
            ViewModelProvider(this).get(MediaControlViewModel::class.java)

        mMediaControlViewModel.nowPlayingSong.observe(this, Observer {
            Log.i("MAINACTIVITY", "New Song Clicked ${it.songName}")
            setUpMediaPlayer(it)
            initializeSeekbar()
        })



        setUpBottomSheet()


        //TODO: CHECK SYNC AUDIO FETCH AND LOADING OF HOME_FRAGMENT


        if (!isDatabaseInitialized() || !permissionGranted()) {
            Toast.makeText(
                this,
                "Fetching Songs from MediaStore for first time",
                Toast.LENGTH_SHORT
            ).show()
            if (!permissionGranted())
                setupPermissions()
            getAudioFiles()
        }
        initUI()

        homeFragment.onPlaySongClickCallback = fun(songEntity: SongEntity) {
//            if (this::mediaPlayer.isInitialized) {
//                mediaPlayer.stop()
//                mediaPlayer.reset()
//                mediaPlayer.release()
//            }
//            val songUri = Uri.parse(songEntity.albumCover)
//            mediaPlayer = MediaPlayer.create(applicationContext, songUri)
//            mediaPlayer.start()

        }

        setUpBottomNav()

        setUpExpandedNowPlaying()

        //initially load  testing playlist fragment
        /*supportFragmentManager.beginTransaction()
                         .replace(
                            R.id.frame, PlaylistsFragment()
                         ).commit()*/


    }

    fun setUpMediaPlayer(songEntity: SongEntity) {
        clearMediaPlayer()
        val songUri = Uri.parse(songEntity.albumCover)
        mediaPlayer = MediaPlayer.create(applicationContext, songUri)
        mediaPlayer.start()
    }

    fun initializeSeekbar() {

        controlSeekBar.max = mediaPlayer.seconds
        txtTotalDuration.text = getDuration(mediaPlayer.seconds.toLong())
        runnable = Runnable {
            controlSeekBar.progress = mediaPlayer.currentSeconds
            txtCurrentDuration.text = getDuration(mediaPlayer.currentSeconds.toLong())
            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
    }

    fun getDuration(seconds: Long): String {
        val time = String.format(
            "%02d : %02d",
            TimeUnit.SECONDS.toMinutes(seconds),
            seconds - TimeUnit.MINUTES.toSeconds(
                TimeUnit.SECONDS.toMinutes(seconds)
            )

        )
        return time
    }

    fun setUpExpandedNowPlaying() {
        btnMinimizeToolbar.setOnClickListener {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            if (bottomNavigationView.selectedItemId == R.id.nowPlaying)
                bottomNavigationView.selectedItemId =
                    R.id.home_button
        }

        controlSeekBar.max = 50
        txtCurrentDuration.text = controlSeekBar.progress.toString()
        txtTotalDuration.text = controlSeekBar.max.toString()


        controlSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progresValue: Int, fromUser: Boolean
                ) {
                    if (fromUser) {
                        mediaPlayer.seekTo(progresValue * 1000)

                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })

        //testing callback for evey state change
        btnRepeatControl.addCheckedStateCallback(
            object :
                RepeatTriStateButton.CheckedStateCallback() {

                override fun onStateChanged(newState: Int) {
                    when (newState) {
                        RepeatTriStateButton.NO_REPEAT -> {
                            Toast.makeText(
                                this@MainActivity,
                                "NO_REPEAT",
                                Toast.LENGTH_SHORT
                            ).show()
//                            testState.text = "NO_REPEAT"
                        }
                        RepeatTriStateButton.REPEAT_ALL -> {
                            Toast.makeText(
                                this@MainActivity,
                                "REPEAT_ALL",
                                Toast.LENGTH_SHORT
                            ).show()
//                            testState.text = "REPEAT_ALL"
                        }
                        RepeatTriStateButton.REPEAT_ONE -> {
                            Toast.makeText(
                                this@MainActivity,
                                "REPEAT_ONE",
                                Toast.LENGTH_SHORT
                            ).show()
//                            testState.text = "REPEAT_ONE"
                        }
                        else -> println("DEFAULT STATE")
                    }
                }

            }
        )

    }

    private suspend fun updateMediaPlayer(mUri: Uri) {
////    private fun updateMediaPlayer(songPath: String) {
//        Log.d("Setting URI", "Inside Update Media Player")
//
//        withContext(Dispatchers.IO) {
//            mediaPlayer?.apply {
//                setDataSource(applicationContext, mUri)
//                prepare()
//                setVolume(0.5f, 0.5f)
//                isLooping = false
//                start()
//            }
//        }

//        mediaPlayer?.setDataSource(songPath)
//        mediaPlayer?.prepare()
//        mediaPlayer?.setVolume(0.5f,0.5f)
//        mediaPlayer?.isLooping = false
//        mediaPlayer?.start()
//            mediaPlayer.apply {
//                setDataSource(applicationContext, mUri)
//                prepare()
//                start()
//            }
    }

    fun initUI() {
        //initially load home_fragment into frame layout...
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.frame,
                homeFragment
//                HomeFragment()
            ).commit()
        //set initial state of bottom sheet
        mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun clearMediaPlayer() {
        if (this::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
            mediaPlayer.reset()
            mediaPlayer.release()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clearMediaPlayer()
    }

    fun setUpBottomNav() {
        //styling bottom navigation
        //get radius
        val radius = resources.getDimension(R.dimen.radius_small)
        //get bottom nav background view
        val bottomNavigationViewBackground =
            bottomNavigationView.background as MaterialShapeDrawable
        //manually change background appearance
        bottomNavigationViewBackground.shapeAppearanceModel =
            bottomNavigationViewBackground.shapeAppearanceModel.toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, radius)
                .build()

        //tab click listeners
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_button -> {
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            HomeFragment()
                        ).commit()
                    true
                }
                R.id.nowPlaying -> {
                    bottomNavigationView.visibility = View.GONE
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    true
                }
                R.id.tab_playlist -> {
                    mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    //TODO : REPLACE WITH PLAYLIST FRAGMENT
                    //testing layout using HomeFragment()
                    supportFragmentManager.beginTransaction().replace(
                        R.id.frame,
                        PlaylistsFragment()
                    ).commit()
                    true
                }
            }
            true

        }

    }

    fun setUpBottomSheet() {
        //define bottomsheet behaviour
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        mBottomSheetBehavior.isDraggable = false


        mBottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            /**
             * Called when the bottom sheet changes its state.
             *
             * @param bottomSheet The bottom sheet view.
             * @param newState The new state. This will be one of [.STATE_DRAGGING], [     ][.STATE_SETTLING], [.STATE_EXPANDED], [.STATE_COLLAPSED], [     ][.STATE_HIDDEN], or [.STATE_HALF_EXPANDED].
             */
            override fun onStateChanged(bottomSheet: View, newState: Int) {

                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        //make bottom nav visible when bottom sheet collapsed
                        bottomNavigationView.visibility = View.VISIBLE
                        b_sheet_Collapsed.visibility = View.VISIBLE
                        b_sheet_Expanded.visibility = View.GONE
                        Toast.makeText(
                            this@MainActivity,
                            "STATE_COLLAPSED",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        //make bottom nav hidden when bottom sheet expanded
                        bottomNavigationView.visibility = View.GONE
                        b_sheet_Collapsed.visibility = View.GONE
                        b_sheet_Expanded.visibility = View.VISIBLE
                        Toast.makeText(
                            this@MainActivity,
                            "STATE_EXPANDED",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        Toast.makeText(
                            this@MainActivity,
                            "STATE_DRAGGING",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                        Toast.makeText(
                            this@MainActivity,
                            "STATE_SETTLING",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        Toast.makeText(
                            this@MainActivity,
                            "STATE_HIDDEN",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    else -> {
                        Toast.makeText(
                            this@MainActivity,
                            "SOME OTHER STATE",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            /**
             * Called when the bottom sheet is being dragged.
             *
             * @param bottomSheet The bottom sheet view.
             * @param slideOffset The new offset of this bottom sheet within [-1,1] range. Offset increases
             * as this bottom sheet is moving upward. From 0 to 1 the sheet is between collapsed and
             * expanded states and from -1 to 0 it is between hidden and collapsed states.
             */
            override fun onSlide(bottomSheet: View, slideOffset: Float) {

                val currentHeight = flFragment.height - bottomSheet.height
                val bottomShiftDown = currentHeight - bottomSheet.top
                flFragment.setPadding(
                    0, 0, 0, bottomSheet.height + bottomShiftDown
                )
            }

        })


        bottomSheet.setOnClickListener {
            if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomNavigationView.visibility = View.GONE
                mBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }


    }

    override fun onBackPressed() {
        //if bottom sheet expanded simply collapse it to show its underlying view automatically
        if (mBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

            //if current selected tab is now playing then set current selected tab to home tab
            if (bottomNavigationView.selectedItemId == R.id.nowPlaying)
                bottomNavigationView.selectedItemId =
                    R.id.home_button

        } else
            when (supportFragmentManager.findFragmentById(R.id.frame)) {
                is SinglePlaylistFragment -> openPlaylistFrag()
                is FavFragment -> openPlaylistFrag()
                !is HomeFragment -> initUI()
                else -> super.onBackPressed()
            }
    }

    fun permissionGranted(): Boolean {
    private fun openPlaylistFrag() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.frame,
                PlaylistsFragment()
            ).commit()
    }

    fun permissionGranted() : Boolean{
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        return (permission == PackageManager.PERMISSION_GRANTED)
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied")
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            READ_STORAGE_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == READ_STORAGE_PERMISSION_REQUEST_CODE) {
            for (i in permissions.indices) {
                val permission: String = permissions[i];
                val grantResult: Int = grantResults[i];

                if (permission == Manifest.permission.READ_EXTERNAL_STORAGE) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        getAudioFiles()
                    }
                }
            }
        }

    }

    private fun getAudioFiles() {

        val songs = mutableListOf<SongEntity>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        // IS_MUSIC : Non-zero if the audio file is music
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"

        // Sort the musics
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"
        //val sortOrder = MediaStore.Audio.Media.TITLE + " DESC

        val contentResolver1 = ContextWrapper(applicationContext).contentResolver

        val cursor = contentResolver1!!.query(
            uri,
            null,
            selection,
            null,
            sortOrder
        )

        //looping through all rows and adding to list
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val songName =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                val artistName =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                val duration =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                val url =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))//.replace("\","\\\"")
                //val url = File(resolver.openFileDescriptor(uri, "r"))

                val songEntity =
                    SongEntity(
                        songs.size + 1,
                        songName,
                        artistName,
                        parseLong(duration),
                        url,
                        -1
                    )
                Log.i("FetchCheck", songEntity.toString())
                songs.add(songEntity)


                //insert song one by one
                //WORKING!!!
//                mAllSongsViewModel.insertSong(songEntity)


            } while (cursor.moveToNext())
        }
        //TODO - To send songs list to DB
        //TODO set shared preferences for isLoaded
        //fetch and initialize db with all songs at once
        //WORKING!!! TO INSERT MULTIPLE SONGS AT ONCE
        mAllSongsViewModel.insertSongs(songs)
        //finally set shared pref
        sharedPreferences.edit().putBoolean("songLoaded", true).apply()
    }

    private fun isDatabaseInitialized(): Boolean =
        sharedPreferences.getBoolean("songLoaded", false)

}