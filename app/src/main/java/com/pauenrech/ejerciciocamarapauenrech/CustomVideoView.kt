package com.pauenrech.ejerciciocamarapauenrech

import android.content.Context
import android.view.MotionEvent
import android.widget.VideoView

class CustomVideoView(context: Context): VideoView(context) {

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }
}