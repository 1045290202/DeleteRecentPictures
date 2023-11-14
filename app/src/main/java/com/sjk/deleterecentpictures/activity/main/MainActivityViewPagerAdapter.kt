package com.sjk.deleterecentpictures.activity.main

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.panpf.zoomimage.ZoomImageView
import com.google.android.material.checkbox.MaterialCheckBox
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.activity.image.ImageActivity
import com.sjk.deleterecentpictures.bean.ImageInfoBean
import com.sjk.deleterecentpictures.common.App
import com.sjk.deleterecentpictures.common.Event
import java.util.ArrayList

class MainActivityViewPagerAdapter(val mainActivity: MainActivity) :
    RecyclerView.Adapter<ViewPagerViewHolder>() {
    
    companion object {
        private const val TAG = "MainActivityViewPagerAdapter"
        lateinit var instance: MainActivityViewPagerAdapter
    }
    
    private var viewPagerViewHolders: MutableList<ViewPagerViewHolder> = ArrayList()
    var imageInfos: MutableList<ImageInfoBean?> = ArrayList()
    var imageChecks: MutableList<Boolean> = ArrayList()
    val event: Event = App.newEvent
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerViewHolder {
        instance = this
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_main_view_pager_item, parent, false)
        val viewPagerViewHolder = ViewPagerViewHolder(this.mainActivity, view)
        viewPagerViewHolders.add(viewPagerViewHolder)
        return viewPagerViewHolder
    }
    
    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        if (this.imageChecks.size > 0) {
            holder.isChecked = if (position < this.imageInfos.size) this.imageChecks[position] else false
        }
        holder.imageInfo = if (position < this.imageInfos.size) {
            this.imageInfos[position]
        } else {
            null
        }
    }
    
    override fun onViewDetachedFromWindow(holder: ViewPagerViewHolder) {
        super.onViewDetachedFromWindow(holder)
        
        App.imageLoadManger.clearImageView(App.applicationContext, holder.imageView)
    }
    
    override fun onViewAttachedToWindow(holder: ViewPagerViewHolder) {
        super.onViewAttachedToWindow(holder)
        
        if (holder.imageInfo?.uri == null) {
            holder.checkBox.visibility = View.GONE
            holder.imageView.visibility = View.GONE
            holder.emptyView.visibility = View.VISIBLE
            return
        }
        holder.checkBox.visibility = View.VISIBLE
        holder.checkBox.isChecked = holder.isChecked
        holder.imageView.visibility = View.VISIBLE
        holder.emptyView.visibility = View.GONE
        App.imageLoadManger.loadImageToImageView(
            App.applicationContext,
            holder.imageInfo!!,
            holder.imageView,
        )
    }
    
    override fun getItemCount(): Int {
        return this.imageInfos.size + 1
    }
    
    fun setHolderChecked(position: Int, isChecked: Boolean) {
        this.viewPagerViewHolders[position].run {
            this.checkBox.isChecked = isChecked
            this.isChecked = isChecked
        }
    }
    
    fun setAllHolderChecked(isChecked: Boolean) {
        this.viewPagerViewHolders.forEachIndexed { index: Int, viewPagerViewHolder: ViewPagerViewHolder ->
            this.setHolderChecked(index, isChecked)
        }
    }
    
}

class ViewPagerViewHolder(val mainActivity: MainActivity, itemView: View) : RecyclerView.ViewHolder(itemView) {
    val checkBox: MaterialCheckBox = itemView.findViewById(R.id.checkbox)
    val imageView: ZoomImageView = itemView.findViewById(R.id.imageView)
    val emptyView: View = itemView.findViewById(R.id.emptyView)
    val detailsButton: Button = itemView.findViewById(R.id.imageDetailsButton)
    private val openImageActivityButton =
        itemView.findViewById<Button>(R.id.openImageActivityButton)
    var imageInfo: ImageInfoBean? = null
    var isChecked: Boolean = false
    
    init {
        this.imageView.scrollBar = null
        this.openImageActivityButton.setOnClickListener {
            if (this.imageInfo?.uri == null) {
                return@setOnClickListener
            }
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this.mainActivity,
                this.mainActivity.findViewById(R.id.imageAnimationView),
                "image"
            )
            val intent = Intent(itemView.context, ImageActivity::class.java)
            itemView.context.startActivity(intent, options.toBundle())
        }
        this.openImageActivityButton.setOnLongClickListener {
            if (this.imageInfo?.uri == null) {
                return@setOnLongClickListener true
            }
            
            App.output.showImageLongClickDialog(this.imageInfo!!.path)
            
            return@setOnLongClickListener true
        }
        this.checkBox.setOnCheckedChangeListener { buttonView: CompoundButton, isChecked: Boolean ->
            if (this.imageInfo?.uri == null) {
                return@setOnCheckedChangeListener
            }
            
            if (App.dataSource.getCurrentImageInfo() != this.imageInfo) {
                return@setOnCheckedChangeListener
            }
            this.isChecked = isChecked
            MainActivityViewPagerAdapter.instance.imageChecks[App.dataSource.getCurrentImageInfoIndex()] =
                isChecked
        }
        this.checkBox.setOnLongClickListener {
            if (this.imageInfo?.uri == null) {
                return@setOnLongClickListener true
            }
            
            MainActivityViewPagerAdapter.instance.setAllHolderChecked(false)
            App.input.setAllImageChecksFalse()
            App.output.showToast(App.applicationContext.getString(R.string.all_selected_images_deselected))
            true
        }
        this.detailsButton.setOnClickListener {
            if (this.imageInfo?.uri == null) {
                return@setOnClickListener
            }
            App.output.showImageDetailsDialog(this.imageInfo)
        }
    }
}