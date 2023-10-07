package com.sjk.deleterecentpictures.activity.main

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.github.piasy.biv.view.BigImageView
import com.github.piasy.biv.view.GlideImageViewFactory
import com.google.android.material.checkbox.MaterialCheckBox
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.activity.image.ImageActivity
import com.sjk.deleterecentpictures.bean.ImageInfoBean
import com.sjk.deleterecentpictures.common.App
import com.sjk.deleterecentpictures.common.Event
import java.util.ArrayList

class MainActivityViewPagerAdapter :
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
        val viewPagerViewHolder = ViewPagerViewHolder(view)
        viewPagerViewHolders.add(viewPagerViewHolder)
        return viewPagerViewHolder
    }
    
    override fun onBindViewHolder(holder: ViewPagerViewHolder, position: Int) {
        if (this.imageChecks.size > 0) {
            holder.isChecked = this.imageChecks[position]
        }
        
        holder.imageInfo = this.imageInfos[position]
    }
    
    override fun onViewDetachedFromWindow(holder: ViewPagerViewHolder) {
        super.onViewDetachedFromWindow(holder)
        
        holder.imageView.cancel()
    }
    
    override fun onViewAttachedToWindow(holder: ViewPagerViewHolder) {
        super.onViewAttachedToWindow(holder)
        
        if (holder.imageInfo?.uri == null) {
            holder.checkBox.visibility = View.GONE
            holder.imageView.visibility = View.GONE
            holder.imageView.cancel()
            return
        }
        holder.checkBox.visibility = View.VISIBLE
        holder.checkBox.isChecked = holder.isChecked
        holder.imageView.visibility = View.VISIBLE
        holder.imageView.showImage(holder.imageInfo!!.uri)
    }
    
    override fun getItemCount(): Int {
        return this.imageInfos.size
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

class ViewPagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val checkBox: MaterialCheckBox = itemView.findViewById(R.id.checkbox)
    val imageView: BigImageView = itemView.findViewById(R.id.imageView)
    val detailsButton: Button = itemView.findViewById(R.id.imageDetailsButton)
    private val openImageActivityButton =
        itemView.findViewById<Button>(R.id.openImageActivityButton)
    var imageInfo: ImageInfoBean? = null
    var isChecked: Boolean = false
    
    init {
        this.imageView.setImageViewFactory(GlideImageViewFactory())
        this.openImageActivityButton.setOnClickListener {
            if (this.imageInfo?.uri == null) {
                return@setOnClickListener
            }
            val intent = Intent(itemView.context, ImageActivity::class.java)
            itemView.context.startActivity(intent)
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
            App.output.showToast("已取消所有已选择图片")
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