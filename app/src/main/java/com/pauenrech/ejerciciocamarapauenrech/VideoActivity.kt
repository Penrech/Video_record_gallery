package com.pauenrech.ejerciciocamarapauenrech

import android.net.Uri
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.MediaController
import android.widget.Toast


import kotlinx.android.synthetic.main.activity_video.*
import android.view.ViewConfiguration
import android.view.MotionEvent





class VideoActivity : AppCompatActivity(),
    CustomMediaController.controllerVisibilityListener{
    private val mHideHandler = Handler()

    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }

    private var videoPath: Uri? = null
    private var mediaController: CustomMediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_video)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        mVisible = true

        val videoUri = intent.getParcelableExtra<Uri>(MainActivity.URI_PATH)
        if (videoUri != null){
            videoPath = videoUri
        } else {
            Toast.makeText(this,"Error cargando video",Toast.LENGTH_LONG).show()
            finish()
        }

        supportActionBar?.setDisplayShowTitleEnabled(false)

        configurarVideoConMediaController()
      /* rootFullscreen.setOnTouchListener { v, event ->
           manageClick()
           true
       }*/
    }

    fun manageClick(){
        if (!mediaController?.isShowing!!){
            mediaController?.show()
        } else {
            mediaController?.hide()
        }
    }

    override fun isVisible(boolean: Boolean) {
        mVisible = !boolean
        toggle()
    }

    fun configurarVideoConMediaController(){
        videoView.setVideoURI(videoPath)
        mediaController = CustomMediaController(this,this)
        mediaController!!.setAnchorView(controller_frame)
        videoView.setMediaController(mediaController)
        videoView.setOnPreparedListener {
            it.seekTo(1)
        }
    }

    fun getMediacontrollerPosition(): Int{
        val hasSoftKey = ViewConfiguration.get(this).hasPermanentMenuKey()

        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0 && !hasSoftKey) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        delayedHide(100)
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        supportActionBar?.hide()

        rootFullscreen.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

    }

    private fun show() {

        rootFullscreen.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION


        supportActionBar?.show()

    }

    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

}
