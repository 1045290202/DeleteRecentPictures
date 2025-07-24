package com.sjk.deleterecentpictures.activity.image

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.github.panpf.zoomimage.ZoomImageView
import com.github.panpf.zoomimage.util.IntOffsetCompat
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.common.App
import com.sjk.deleterecentpictures.entity.ImageInfoEntity
import kotlinx.coroutines.launch


class ImageActivityViewPagerAdapter :
    RecyclerView.Adapter<ViewPagerViewHolder>() {
    private val viewPagerViewHolders: MutableList<ViewPagerViewHolder> = ArrayList()
    private val attachedViewHolders: MutableSet<ViewPagerViewHolder> = mutableSetOf()
    var imageInfos: List<ImageInfoEntity?> = ArrayList()

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

        this.attachedViewHolders.remove(holder)
        App.imageLoadManger.clearImageView(App.applicationContext, holder.imageView)
    }

    override fun onViewAttachedToWindow(holder: ViewPagerViewHolder) {
        super.onViewAttachedToWindow(holder)

        this.attachedViewHolders.add(holder)
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

    fun isCurrentScaleOne(): Boolean {
        for (it in this@ImageActivityViewPagerAdapter.attachedViewHolders) {
            // 判断x就行了，暂时没有xy缩放不一致的情况
            if (it.imageView.zoomable.transformState.value.scaleX != 1f) {
                return false
            }
        }
        return true
    }

    /**
     * 重置图片缩放
     */
    fun resetImageScaleWithAnimation(lifecycleOwner: ComponentActivity) {
        lifecycleOwner.lifecycleScope.launch {
            this@ImageActivityViewPagerAdapter.attachedViewHolders.forEach {
                it.imageView.zoomable.scale(
                    1f,
                    IntOffsetCompat.Zero,
                    true,
                    // ZoomAnimationSpec(400),
                )
            }
        }
    }

}

class ViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ZoomImageView = itemView.findViewById(R.id.imageView)
    val preventClickLeftView: View = itemView.findViewById(R.id.preventClickLeftView)
    val preventClickRightView: View = itemView.findViewById(R.id.preventClickRightView)
    var imageInfo: ImageInfoEntity? = null

    init {
        this.imageView.scrollBar = null
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
//
//        val gestureDetector =
//            GestureDetector(itemView.context, object : GestureDetector.SimpleOnGestureListener() {
////                override fun onFling(
////                    e1: MotionEvent?,
////                    e2: MotionEvent,
////                    velocityX: Float,
////                    velocityY: Float
////                ): Boolean {
////                    App.output.showToast("onFling$velocityY")
////                    imageView.post {
////                        if (velocityX > 0) {
////                            (itemView.context as ImageActivity).onBackPressedDispatcher.onBackPressed()
////                        }
////                    }
////                    return super.onFling(e1, e2, velocityX, velocityY)
////                }
//
//                override fun onScroll(
//                    e1: MotionEvent?,
//                    e2: MotionEvent,
//                    distanceX: Float,
//                    distanceY: Float
//                ): Boolean {
//                    if (imageView.zoomable.transformState.value.scaleX != 1f) {
//                        return super.onScroll(e1, e2, distanceX, distanceY)
//                    }
//                    // 判断是不是双指
//                    if (e1!!.pointerCount >= 2) {
//                        return super.onScroll(e1, e2, distanceX, distanceY)
//                    }
//                    App.output.showToast("滑动$distanceY")
//                    imageView.post {
//                        if (distanceY > 0) {
//                            (itemView.context as ImageActivity).onBackPressedDispatcher.onBackPressed()
//                        }
//                    }
//                    return true
//                }
//            })
//
//        this.imageView.setOnTouchListener { _, event ->
//            gestureDetector.onTouchEvent(event)
//            false
//        }
    }
}