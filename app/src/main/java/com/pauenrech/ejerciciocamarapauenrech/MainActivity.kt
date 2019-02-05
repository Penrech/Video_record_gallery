package com.pauenrech.ejerciciocamarapauenrech

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v4.content.ContextCompat
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.video_card.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(),
    GalleryRecyclerAdapter.sendClickListener{

    companion object {
        val VIDEO_TRANSITION_IMAGE = "video_trans_img"
        val VIDEO_TRANSITION_CARD = "video_trans_card"
        val TRANSITION_NAME_IMAGE = "transition_img"
        val TRANSITION_NAME_CARD = "transition_card"
        val URI_PATH = "uriPath"
    }

    private val WRITE_REQUEST_CODE = 101
    private val VIDEO_INTENT = 101

    private var permisoAceptado: Boolean = false

    private var adapter: GalleryRecyclerAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? = null

    private var videos: MutableList<Video> = mutableListOf()

    private val videosFolderName = "CamaraEnrechVideos"
    private var videosFolder: File? = null
    private var newVideo: File? = null
    private var newVideoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.title = "Video Galeria"

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        getNumberOfColumns()
        requestPermission()
    }

    private fun getNumberOfColumns(){
        val width = resources.displayMetrics.widthPixels
        val dpWith = width / resources.displayMetrics.density
        val scale = 100
        val numOfColumns: Int = (dpWith / scale).toInt()
        val newMeasuresDp = (dpWith / numOfColumns)
        val newMeasuresPixel = newMeasuresDp * resources.displayMetrics.density
        val newPadding = (newMeasuresPixel * 0.05)

        layoutManager = GridLayoutManager(this,numOfColumns)
        galleryRV.layoutManager = layoutManager
        galleryRV.addItemDecoration(SpacesItemDecoration(newPadding.toInt(),numOfColumns))

        if (adapter == null){
            adapter = GalleryRecyclerAdapter(this,videos,newMeasuresPixel.toInt(),newMeasuresDp.toInt(),this)
            galleryRV.adapter = adapter
        } else{
            adapter!!.notifyDataSetChanged()
        }

    }

    private fun goToVideo(video: Video){
        val transitionVideoImg = VIDEO_TRANSITION_IMAGE + video.id
        val transitionVideoRoot = VIDEO_TRANSITION_CARD + video.id

        val itemView = galleryRV.layoutManager!!.findViewByPosition(adapter?.getPositionFromId(video.id)!!)
        val videoView = itemView!!.videoThumb
        val rootView = itemView.rootCard

        val pairImg = Pair<View,String>(videoView,transitionVideoImg)
        val pairBackground = Pair<View,String>(rootView,transitionVideoRoot)

        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@MainActivity,pairImg, pairBackground)

        val intent = Intent(this,VideoActivity::class.java)
        intent.putExtra(URI_PATH,video.uri)
        intent.putExtra(TRANSITION_NAME_IMAGE, transitionVideoImg)
        intent.putExtra(TRANSITION_NAME_CARD,transitionVideoRoot)
        startActivity(intent,options.toBundle())
    }

    fun startCamera(){
        if (comprobarCamara()) {
            if (permisoAceptado == true) {

                val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

                newVideo = File(videosFolder, timestamp + ".mp4")
                newVideoUri =
                        FileProvider.getUriForFile(this, "com.pauenrech.ejerciciocamarapauenrech.provider", newVideo!!)

                intent.putExtra(MediaStore.EXTRA_OUTPUT, newVideoUri)
                startActivityForResult(intent, VIDEO_INTENT)
            } else {
                Toast.makeText(this,"Esta aplicacion necesita permisos de escritura para funcionar",Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this,"Este dispositivo no dispone de cámara",Toast.LENGTH_LONG).show()
        }
    }

    private fun comprobarCamara(): Boolean{
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }


    private fun enviarAVideoActivity(uri: Uri){
        val intent = Intent(this,VideoActivity::class.java)

        intent.putExtra(URI_PATH,uri)
        startActivity(intent)
    }


    fun getAllVideosFromGallery(){
        videos.clear()
        val projection = arrayOf(MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID, MediaStore.Video.Media.HEIGHT,MediaStore.Video.Media.WIDTH)
        val orderBy = MediaStore.Video.Media._ID + " DESC"
        val selection = MediaStore.Video.VideoColumns.DATA + " like?"
        val selectionArgs: Array<String> = arrayOf("%$videosFolderName%")

        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection,
            selection, selectionArgs, orderBy
        )

        if (cursor.moveToFirst()){
            do{
                val videoPath = cursor.getString(0)
                val videoID = cursor.getInt(1)
                val videoH = cursor.getInt(2)
                val videoW = cursor.getInt(3)
                val video = Video(Uri.parse(videoPath),videoID,videoH,videoW)
                videos.add(video)
            } while (cursor.moveToNext())
        }

        adapter!!.notifyDataSetChanged()
        cursor.close()
    }


    private fun requestPermission(){
        val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permiso == PackageManager.PERMISSION_GRANTED){
            permisoAceptado = true
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            videosFolder = File(path,videosFolderName)
            videosFolder!!.mkdirs()
            getAllVideosFromGallery()
        } else {
            permisoAceptado = false
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_REQUEST_CODE)
        }
    }

    override fun onVideoClickListener(video: Video) {
            goToVideo(video)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            WRITE_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"No podemos mostrar la galeria", Toast.LENGTH_LONG).show()
                }
                else{
                    requestPermission()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == VIDEO_INTENT){
            if (resultCode == Activity.RESULT_OK){

                MediaScannerConnection.scanFile(
                    this,
                    arrayOf(newVideo?.toString()), null
                ) { path, uri ->
                    Log.i("TAG","Devuelve path: $path y uri $uri")
                    runOnUiThread {
                        getAllVideosFromGallery()
                        newVideo = null
                        newVideoUri = null
                        enviarAVideoActivity(Uri.parse(path))
                    }
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
           R.id.navigation_camera -> {
                startCamera()
           }
        }
        false
    }
}
