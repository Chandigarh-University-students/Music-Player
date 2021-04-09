package com.projects.musicplayer.uicomponents

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.InsetDrawable
import android.util.AttributeSet
import android.widget.CheckBox
import androidx.core.content.ContextCompat
import com.projects.musicplayer.R
import java.util.ArrayList

@SuppressLint("AppCompatCustomView")
class RepeatTriStateButton(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    CheckBox(context, attrs, defStyleAttr) {
    companion object {
        val NO_REPEAT = 0
        val REPEAT_ALL = 1
        val REPEAT_ONE = 2
    }

    private val callbacks = ArrayList<CheckedStateCallback>()

    var state = NO_REPEAT
        private set(value) {
            field = value
            updateBtn()
        }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null, 0)

    init {
        state = NO_REPEAT
        updateBtn()

        setOnCheckedChangeListener { buttonView, isChecked ->
            state = when (state) {
                NO_REPEAT -> REPEAT_ALL
                REPEAT_ALL -> REPEAT_ONE
                REPEAT_ONE -> NO_REPEAT
                else -> NO_REPEAT
            }
            updateBtn()
            callbacks.forEach {
                it.onStateChanged(state)
            }
        }

    }

    abstract class CheckedStateCallback {
        abstract fun onStateChanged(newState: Int)
    }

    fun addCheckedStateCallback(callback: CheckedStateCallback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback)
        }
    }

    private fun updateBtn() {
        val btnDrawable = when (state) {
            NO_REPEAT -> R.drawable.ic_no_repeat
            REPEAT_ALL -> R.drawable.ic_repeat_all
            REPEAT_ONE -> R.drawable.ic_repeat_one
            else -> R.drawable.ic_no_repeat
        }

        val styledDrawable =
            InsetDrawable(ContextCompat.getDrawable(context, btnDrawable), 4, 4, 4, 4)

        buttonDrawable = styledDrawable

    }

}