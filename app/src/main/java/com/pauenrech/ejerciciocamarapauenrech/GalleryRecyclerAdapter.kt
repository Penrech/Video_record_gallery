package com.pauenrech.ejerciciocamarapauenrech

import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.video_card.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestOptions
import java.io.File

class GalleryRecyclerAdapter(val context: Context,
                             var listaVideos: MutableList<Video>,
                             val cardSize: Int,
                             val imageSize: Int,
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

    fun getPositionFromId(id: Int): Int{
        val elementFromList = listaVideos.filter { it.id == id }
        return listaVideos.indexOf(elementFromList[0])
    }

    override fun onBindViewHolder(p0: GalleryVH, p1: Int) {
        val video = listaVideos[p1]
        p0.video = video
        p0.imageView.transitionName = MainActivity.VIDEO_TRANSITION_IMAGE + "${listaVideos[p1].id}"
        p0.rootCard.transitionName = MainActivity.VIDEO_TRANSITION_CARD + "${listaVideos[p1].id}"

        Glide
            .with(context)
            .asBitmap()
                .load(Uri.fromFile(File(video.uri.path)))
                .apply(RequestOptions().override(cardSize,cardSize).centerCrop())
            .transition(BitmapTransitionOptions.withCrossFade())
            .into(p0.imageView)
    }


    inner class GalleryVH(itemView: View): RecyclerView.ViewHolder(itemView){
        val imageView = itemView.videoThumb
        val rootCard = itemView.rootCard
        var video: Video? = null

        init {
            imageView.setOnClickListener {
                clickListener.onVideoClickListener(video!!)
            }
        }
    }

}
