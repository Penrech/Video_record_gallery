package com.pauenrech.ejerciciocamarapauenrech

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.BottomNavigationView
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(),
    GalleryRecyclerAdapter.sendClickListener{

    companion object {
        val URI_PATH = "uriPath"
    }

    private val APP_DIRECTORY = "EjercicioVideoPauEnrech"
    private val WRITE_REQUEST_CODE = 101
    private val VIDEO_INTENT = 101
    private var videoPath: Uri? = null
    private var videoPlaying: Boolean = false
    private var appVideoFolder: String? = null

    private var adapter: GalleryRecyclerAdapter? = null
    private var layoutManager: RecyclerView.LayoutManager? = null

    private var videoCount: Int? = null
    private var videos: MutableList<Video> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        layoutManager = GridLayoutManager(this,numOfColumns)
        galleryRV.layoutManager = layoutManager

        if (adapter == null){
            adapter = GalleryRecyclerAdapter(this,videos,newMeasuresPixel.toInt(),newMeasuresDp.toInt(), numOfColumns,this)
            galleryRV.adapter = adapter
        } else{
            adapter!!.notifyDataSetChanged()
        }

    }

    private fun comprobarCamara(): Boolean{
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    private fun goToVideo(video: Video){
        val intent = Intent(this,VideoActivity::class.java)
        intent.putExtra(URI_PATH,video.uri)
        startActivity(intent)
    }

    fun startCamera(view: View){
        if (comprobarCamara()) {
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            startActivityForResult(intent,VIDEO_INTENT)
        } else {
            Toast.makeText(this,"Este dispositivo no dispone de cámara",Toast.LENGTH_LONG).show()
        }
    }

    private fun enviarAVideoActivity(uri: Uri){
        val intent = Intent(this,VideoActivity::class.java)
        intent.putExtra(URI_PATH,uri)
        startActivity(intent)
    }

    fun getAllVideosFromGallery(){
        val projection = arrayOf(MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID, MediaStore.Video.Media.HEIGHT,MediaStore.Video.Media.WIDTH)
        val orderBy = MediaStore.Video.Media._ID

        val cursor = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection,
            null, null, orderBy
        )

        //videoCount = cursor!!.count

        if (!cursor.moveToFirst()){

        } else {
            do{
                val videoPath = cursor.getString(0)
                val videoID = cursor.getInt(1)
                val videoH = cursor.getInt(2)
                val videoW = cursor.getInt(3)
                val video = Video(Uri.parse(videoPath),videoID,videoH,videoW)
                videos.add(video)
            } while (cursor.moveToNext())
            adapter!!.notifyDataSetChanged()
        }

        cursor.close()
    }


    private fun requestPermission(){
        val permiso = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permiso == PackageManager.PERMISSION_GRANTED){
            getAllVideosFromGallery()
        } else {
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
                if (data != null){
                    videoPath = data.data
                    enviarAVideoActivity(videoPath!!)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
           R.id.navigation_camera -> {

           }
        }
        false
    }
}
