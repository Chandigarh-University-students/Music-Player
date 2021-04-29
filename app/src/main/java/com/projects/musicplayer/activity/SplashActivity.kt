package com.projects.musicplayer.activity

import android.Manifest
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.projects.musicplayer.R

class SplashActivity : AppCompatActivity() {

    private val READ_STORAGE_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if(!permissionGranted())
            setupPermissions()
        else {
            startActivity(
                Intent(
                    this@SplashActivity,
                    MainActivity::class.java
                )
            )
            finish()
        }
    }

    fun permissionGranted(): Boolean {
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
            Log.i("Req", "Permission to record denied")
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            1
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            1 -> {
                Log.i("Req", "$requestCode Requested read storage")
                if (requestCode == READ_STORAGE_PERMISSION_REQUEST_CODE) {
                    for (i in permissions.indices) {
                        val permission: String = permissions[i];
                        val grantResult: Int = grantResults[i];

                        if (permission == Manifest.permission.READ_EXTERNAL_STORAGE) {
                            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                                Log.i("Req", "Permission granted for read storage")
                                    startActivity(
                                        Intent(this@SplashActivity,
                                            MainActivity::class.java)
                                    )
                                    finish()
                            }
                            else {
                                Log.i("Req", "Permission denied for read storage")
                                Toast.makeText(this,"Storage permission required", Toast.LENGTH_LONG).show()
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
}