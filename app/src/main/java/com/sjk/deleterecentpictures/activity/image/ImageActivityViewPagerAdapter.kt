package com.sjk.deleterecentpictures.activity.image

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import androidx.recyclerview.widget.RecyclerView
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.github.panpf.zoomimage.view.zoom.ZoomAnimationSpec
import com.github.panpf.zoomimage.zoom.GestureType
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.bean.ImageInfoBean
import com.sjk.deleterecentpictures.common.App


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
        position: Int,
    ) {
        holder.imageInfo = imageInfos[position]
    }
    
    override fun onViewDetachedFromWindow(holder: ViewPagerViewHolder) {
        super.onViewDetachedFromWindow(holder)
        
        App.imageLoadManger.clearImageView(App.applicationContext, holder.imageView)
    }
    
    override fun onViewAttachedToWindow(holder: ViewPagerViewHolder) {
        super.onViewAttachedToWindow(holder)
        
        if (holder.imageInfo?.uri == null) {
            return
        }
        App.imageLoadManger.loadImageToImageView(
            App.applicationContext,
            holder.imageInfo!!,
            holder.imageView,
        )
        // holder.imageView.setImageURI(holder.imageInfo?.uri)
    }
    
    override fun getItemCount(): Int {
        return this.imageInfos.size
    }
    
    fun resetImageScaleWithAnimation(viewPager: ViewPager2) {
        this.viewPagerViewHolders.forEach {
            it.imageView.zoomable.scale(
                1f, IntOffsetCompat.Zero, false,
                // ZoomAnimationSpec(40)
            )
        }
    }
    
}

class ViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ZoomImageView = itemView.findViewById(R.id.imageView)
    val longClickView: View = itemView.findViewById(R.id.longClickView)
    val preventClickLeftView: View = itemView.findViewById(R.id.preventClickLeftView)
    val preventClickRightView: View = itemView.findViewById(R.id.preventClickRightView)
    var imageInfo: ImageInfoBean? = null
    
    init {
        this.imageView.scrollBar = null
        this.imageView.zoomable.disabledGestureTypeState.value = GestureType.ONE_FINGER_SCALE
        this.imageView.setOnLongClickListener {
            App.output.showImageLongClickDialog(this.imageInfo!!.path)

            return@setOnLongClickListener true
        }
        this.imageView.setOnClickListener {
            (this.itemView.context as ImageActivity).onBackPressedDispatcher.onBackPressed()
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