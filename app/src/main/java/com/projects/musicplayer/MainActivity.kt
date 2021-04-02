package com.projects.musicplayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable

class MainActivity : AppCompatActivity() {
    lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

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

        //initially load  testing playlist fragment
       /*supportFragmentManager.beginTransaction()
                        .replace(
                           R.id.frame, Playlists()
                        ).commit()*/


        //initially load home_fragment into frame layout...
        supportFragmentManager.beginTransaction()
                        .replace(R.id.frame, HomeFragment()).commit()

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_button -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(
//                            R.id.frame, HOME_FRAGMENT()
//                        ).commit()
//                    true
                }
                R.id.nowPlaying -> {
                    //TODO: Replace with MotionLayout
                    true
                }
                R.id.tab_playlist -> {
//                    supportFragmentManager.beginTransaction().replace(
//                        R.id.frame, PLAYLIST_FRAGMENT
//                    ).commit()
//                    true
                }
            }
            true

        }
    }
}