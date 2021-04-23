package com.projects.musicplayer.uicomponents

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import com.projects.musicplayer.R

class CustomDialog(
    context: Context
) : Dialog(context) {
    var positiveButton: TextView? = null
    var negativeButton: TextView? = null
    var etInput: EditText? = null

    var positiveButtonCallback: ((playlistName: String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.playlist_dialog)
        positiveButton = findViewById(R.id.btnOK)
        negativeButton = findViewById(R.id.btnCancel)
        etInput = findViewById(R.id.etInput)
        negativeButton!!.setOnClickListener {
            dismiss()
        }

        positiveButton!!.setOnClickListener {
            positiveButtonCallback?.invoke(etInput!!.text.toString())
            dismiss()

        }

        setOnDismissListener {
            etInput!!.text.clear()
        }
    }

}