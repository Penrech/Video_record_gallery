package com.pauenrech.ejerciciocamarapauenrech

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.MediaController

class CustomMediaController(context: Context,
                            val listener: CustomMediaController.controllerVisibilityListener): MediaController(context) {

    interface controllerVisibilityListener{
        fun isVisible(boolean: Boolean)
    }


    override fun show() {
        Log.i("TAG","Controller visible")
            super.show()
            listener.isVisible(true)

    }

    override fun hide() {
        Log.i("TAG","Controller invisible")
            super.hide()
            listener.isVisible(false)

    }
}