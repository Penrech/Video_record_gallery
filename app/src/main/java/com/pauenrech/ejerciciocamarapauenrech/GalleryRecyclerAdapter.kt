package com.pauenrech.ejerciciocamarapauenrech

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.AsyncTask
import android.provider.MediaStore
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.video_card.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestOptions
import java.io.File


class GalleryRecyclerAdapter(val context: Context,
                             var listaVideos: MutableList<Video>,
                             val cardSize: Int,
                             val imageSize: Int,
                             val columNumber: Int,
                             val clickListener: sendClickListener): RecyclerView.Adapter<GalleryRecyclerAdapter.GalleryVH>() {

    interface sendClickListener{
        fun onVideoClickListener(video: Video)
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): GalleryVH {
        val celda = LayoutInflater.from(context).inflate(R.layout.video_card, p0, false)
        return GalleryVH(celda)
    }

    override fun getItemCount(): Int {
       return listaVideos.size
    }

    override fun onBindViewHolder(p0: GalleryVH, p1: Int) {
        val originId = listaVideos[p1].id
        val uri = listaVideos[p1].uri

        val video = listaVideos[p1]
        p0.video = video

        Glide
            .with(context)
            .asBitmap()
                .load(Uri.fromFile(File(video.uri.path)))
                .apply(RequestOptions().override(imageSize,imageSize).centerCrop())
            .transition(BitmapTransitionOptions.withCrossFade())
            .into(p0.imageView)
        p0.setSize(cardSize, p1)
    }


    inner class GalleryVH(itemView: View): RecyclerView.ViewHolder(itemView){
        val imageView = itemView.videoThumb
        val rootCard = itemView.rootCard
        var video: Video? = null

        fun setSize(size: Int, position: Int){
            val lastRow = itemCount / columNumber
            val params = rootCard.layoutParams

            val newSize: Int = size
            val newPadding: Int = (size * 0.05).toInt()
            val currentColumn = position % columNumber
            val currentRow = position / columNumber

            params.width = newSize
            params.height = newSize
            rootCard.layoutParams = params

            when{
                currentColumn == 0 && currentRow == lastRow -> {
                    rootCard.setPadding(newPadding,newPadding,newPadding,newPadding)
                }
                currentColumn == 0 && currentRow < lastRow -> {
                    rootCard.setPadding(newPadding,newPadding,newPadding,0)
                }
                currentColumn > 0 && currentRow < lastRow -> {
                    rootCard.setPadding(0,newPadding,newPadding,0)
                }
                currentColumn > 0 && currentRow == lastRow -> {
                    rootCard.setPadding(0,newPadding,newPadding,newPadding)
                }
            }


        }

        init {
            imageView.setOnClickListener {
                clickListener.onVideoClickListener(video!!)
            }
        }
    }

}
