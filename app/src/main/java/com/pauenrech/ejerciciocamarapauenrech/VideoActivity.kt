package com.pauenrech.ejerciciocamarapauenrech

import android.app.Activity
import android.media.MediaPlayer
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.transition.TransitionInflater
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_video.*
import android.widget.SeekBar
import com.bumptech.glide.Glide
import java.io.File


class VideoActivity : AppCompatActivity(),
    SeekBar.OnSeekBarChangeListener{

    private val mHideHandler = Handler()
    private var mediaPlayer: MediaPlayer? = null
    private var videoRunnable: Runnable? = null

    private var utilities: Utility? = null

    private val mHidePart2Runnable = Runnable {
        fullscreen_content.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        mediaPlayer?.let {
            if (it.isPlaying){
                playPauseButton.visibility = View.GONE
            }
        }
    }

    private val mShowPart2Runnable = Runnable {
        supportActionBar?.show()
        playPauseButton.visibility = View.VISIBLE
        fullscreen_content_controls.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }

    private var videoPath: Uri? = null
    private var videoPositionOnActivityPause: Int = 0
    private var videoTransition : String? = null
    private var cardTransition: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_video)

        manageTransition()

        val transitionEnter = TransitionInflater.from(this).inflateTransition(R.transition.enter_shared_transition)
        window.sharedElementEnterTransition = transitionEnter

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        utilities = Utility()

        videoSeekbar.setOnSeekBarChangeListener(this)

        setRunnable()
        setUpVideo()

        mVisible = true

        fullscreen_content.setOnClickListener{ toggle() }
        playPauseButton.setOnClickListener{
            playPauseVideo(!mediaPlayer!!.isPlaying)
        }

    }

    private fun manageTransition(){
        videoPath = intent.getParcelableExtra(MainActivity.URI_PATH)
        if (videoPath == null){
            Toast.makeText(this,"Error cargando video",Toast.LENGTH_LONG).show()
            supportFinishAfterTransition()
        }

        Glide.with(this)
            .asDrawable()
            .load(Uri.fromFile(File(videoPath!!.path)))
            .into(videoBackground)

        videoTransition = intent.getStringExtra(MainActivity.TRANSITION_NAME_IMAGE)
        cardTransition = intent.getStringExtra(MainActivity.TRANSITION_NAME_CARD)
        videoTransition?.let {
            videoBackground.transitionName = videoTransition
        }


        videoBackground.setOnSystemUiVisibilityChangeListener {
            mediaPlayer?.seekTo(1)
        }

    }

    private fun setUpVideo(){

        videoView.setVideoURI(videoPath)
        videoView.setOnPreparedListener {
            mediaPlayer = it

            totalTime.text = utilities!!.formatSecondsToString(it.duration)

            it.setOnCompletionListener {
                Log.i("TAG","Completado")
                restartVideo()
            }

            if (videoSeekbar.progress > 0) {
                it.seekTo(videoPositionOnActivityPause.toLong(), MediaPlayer.SEEK_CLOSEST)
            }

            videoSeekbar.max = 100
        }
    }

    private fun restartVideo(){
        mediaPlayer?.let {
            if (it.isPlaying)
                it.pause()
        }
        show()
        setUpPlayButton()
        mediaPlayer?.seekTo(1)
        updateProgressBar()
        videoSeekbar.progress = 0
        cancelDelayedHide()
        videoBackground.visibility = View.VISIBLE
        AUTO_HIDE = false
    }

    private fun setUpPlayButton(){
        if (mediaPlayer!!.isPlaying)
            playPauseButton.setImageDrawable(getDrawable(R.drawable.ic_round_pause_circle_filled_100))
        else
            playPauseButton.setImageDrawable(getDrawable(R.drawable.ic_round_play_circle_filled_white_100))
    }

    private fun playPauseVideo(play: Boolean){
        if (videoBackground.visibility == View.VISIBLE){
            videoBackground.visibility = View.INVISIBLE
            }
        if (play){
            videoView.start()
            //videoSeekbar.progress = utilities!!.getProgressPercentage(mediaPlayer!!.currentPosition, mediaPlayer!!.duration)
            updateProgressBar()
            setUpPlayButton()
            AUTO_HIDE = true
            hide()


        } else {
            videoView.pause()
            mHideHandler.removeCallbacks(videoRunnable)
            setUpPlayButton()
            cancelDelayedHide()
            AUTO_HIDE = false
            show()

        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        mHideHandler.removeCallbacks(videoRunnable)
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        mHideHandler.removeCallbacks(videoRunnable)
        val totalDuration = mediaPlayer?.duration
        val currentPosition = utilities!!.progressToTimer(seekBar!!.progress, totalDuration!!)

        mediaPlayer?.seekTo(currentPosition.toLong(), MediaPlayer.SEEK_CLOSEST)

        updateProgressBar()

    }

    fun updateProgressBar(){

        mHideHandler.removeCallbacksAndMessages(videoRunnable)
        mHideHandler.postDelayed(videoRunnable, 100)
    }

    fun setRunnable() {
        videoRunnable = Runnable {

                val totalDuration = mediaPlayer?.duration
                val currentDuration = mediaPlayer?.currentPosition

                totalTime.text = utilities!!.formatSecondsToString(totalDuration!!)
                currentTime.text = utilities!!.formatSecondsToString(currentDuration!!)

                val progress = utilities!!.getProgressPercentage(currentDuration,totalDuration)
                videoSeekbar.progress = progress

                if (mediaPlayer?.isPlaying!!)
                    mHideHandler.postDelayed(videoRunnable, 100)
        }
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
            if (AUTO_HIDE)
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
    }

    private fun hide() {
        supportActionBar?.hide()
        fullscreen_content_controls.visibility = View.GONE

        mVisible = false

        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        fullscreen_content.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    private fun cancelDelayedHide(){
        mHideHandler.removeCallbacks(mHideRunnable)
    }

    companion object {
        private var AUTO_HIDE = false
        private val AUTO_HIDE_DELAY_MILLIS = 3000
        private val UI_ANIMATION_DELAY = 300
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home ->{
                onBackPressed()
                true
            }
            else -> true
        }
    }

    override fun onPause() {
        super.onPause()
        mHideHandler.removeCallbacks(videoRunnable)
        mediaPlayer?.let {
            if (it.isPlaying) {
                playPauseVideo(false)
            }
            videoPositionOnActivityPause = mediaPlayer!!.currentPosition
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mHideHandler.removeCallbacks(videoRunnable)
    }

    override fun onBackPressed() {
        mediaPlayer?.let {
            if (it.currentPosition > 1){
                restartVideo()
                return
            }
        }
        videoBackground.visibility = View.VISIBLE
        videoView.stopPlayback()
        mediaPlayer?.release()
        mediaPlayer = null
        rootVideo.removeView(controles)
        rootVideo.removeView(playPauseButton)
        rootVideo.removeView(fullscreen_content)

        if (videoTransition != null){
            rootVideo.transitionName = cardTransition
            supportFinishAfterTransition()
        } else {
            finish()
        }

    }

    override fun supportFinishAfterTransition() {
        super.supportFinishAfterTransition()
    }


}
