package com.sjk.deleterecentpictures.activity.image

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.piasy.biv.view.BigImageView
import com.github.piasy.biv.view.GlideImageViewFactory
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.bean.ImageInfoBean
import com.sjk.deleterecentpictures.common.App
import java.util.ArrayList

class ImageActivityViewPagerAdapter :
    RecyclerView.Adapter<ViewPagerViewHolder>() {
    private var viewPagerViewHolders: MutableList<ViewPagerViewHolder> = ArrayList()
    var imageInfos: List<ImageInfoBean?> = ArrayList()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        val viewPagerViewHolder = ViewPagerViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_view_pager_item, parent, false)
        )
        viewPagerViewHolders.add(viewPagerViewHolder)
        return viewPagerViewHolder
    }
    
    override fun onBindViewHolder(
        holder: ViewPagerViewHolder,
        position: Int
    ) {
        holder.imageInfo = imageInfos[position]
    }
    
    override fun onViewDetachedFromWindow(holder: ViewPagerViewHolder) {
        super.onViewDetachedFromWindow(holder)
        
        holder.imageView.cancel()
    }
    
    override fun onViewAttachedToWindow(holder: ViewPagerViewHolder) {
        super.onViewAttachedToWindow(holder)
        
        if (holder.imageInfo?.uri == null) {
            return
        }
        holder.imageView.showImage(holder.imageInfo!!.uri)
    }
    
    override fun getItemCount(): Int {
        return this.imageInfos.size
    }
    
}

class ViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: BigImageView = itemView.findViewById(R.id.imageView)
    val preventClickLeftView: View = itemView.findViewById(R.id.preventClickLeftView)
    val preventClickRightView: View = itemView.findViewById(R.id.preventClickRightView)
    var imageInfo: ImageInfoBean? = null
    
    init {
        this.imageView.setImageViewFactory(GlideImageViewFactory())
        this.imageView.setOnLongClickListener {
            App.output.showImageLongClickDialog(this.imageInfo!!.path)
            
            return@setOnLongClickListener true
        }
        // this.preventClickLeftView.setOnClickListener {
        //
        // }
        this.preventClickLeftView.setOnLongClickListener {
            return@setOnLongClickListener true
        }
        // this.preventClickRightView.setOnClickListener {
        //
        // }
        this.preventClickRightView.setOnLongClickListener {
            return@setOnLongClickListener true
        }
    }
    
}