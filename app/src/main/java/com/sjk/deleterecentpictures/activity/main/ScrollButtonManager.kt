package com.sjk.deleterecentpictures.activity.main

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.widget.Button
import com.sjk.deleterecentpictures.R
import com.sjk.deleterecentpictures.common.Const
import com.sjk.deleterecentpictures.common.logD
import java.util.Timer
import java.util.TimerTask

object ScrollButtonManager {
    private const val TAG = "ScrollButtonManager"
    
    private lateinit var mainActivity: MainActivity
    // private var autoScrollStopped = true
    private var autoScrollTimer: Timer? = null
    
    fun init(activity: MainActivity) {
        this.mainActivity = activity
        this.initNextButtonEvent()
        this.initPreviousButtonEvent()
    }
    
    fun stopAutoScroll() {
        this.autoScrollTimer?.cancel()
        this.autoScrollTimer = null
    }
    
    /**
     * 开始往后自动滚动
     */
    fun startAutoScrollNext() {
        logD(TAG, "startAutoScrollNext")
        this.stopAutoScroll()
        this.autoScrollTimer = Timer()
        this.autoScrollTimer!!.schedule(object : TimerTask() {
            override fun run() {
                this@ScrollButtonManager.mainActivity.runOnUiThread {
                    this@ScrollButtonManager.mainActivity.jumpToNextImage()
                }
            }
        }, Const.AUTO_SCROLL_DELAY, Const.AUTO_SCROLL_INTERVAL)
    }
    
    /**
     * 开始往前自动滚动
     */
    fun startAutoScrollPrevious() {
        logD(TAG, "startAutoScrollPrevious")
        this.stopAutoScroll()
        this.autoScrollTimer = Timer()
        this.autoScrollTimer!!.schedule(object : TimerTask() {
            override fun run() {
                this@ScrollButtonManager.mainActivity.runOnUiThread {
                    this@ScrollButtonManager.mainActivity.jumpToPreviousImage()
                }
            }
        }, Const.AUTO_SCROLL_DELAY, Const.AUTO_SCROLL_INTERVAL)
    }
    
    @SuppressLint("ClickableViewAccessibility")
    private fun initNextButtonEvent() {
        var startTime = 0L
        
        val nextButton = this.mainActivity.findViewById<Button>(R.id.nextButton)
        // nextButton.setOnClickListener {
        //     this@ScrollButtonManager.mainActivity.jumpToNextImage()
        // }
        nextButton.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    startTime = System.currentTimeMillis()
                    this.startAutoScrollNext()
                }
                
                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_UP,
                -> {
                    this.stopAutoScroll()
                    if (System.currentTimeMillis() - startTime < Const.AUTO_SCROLL_DELAY) {
                        this.mainActivity.jumpToNextImage()
                    }
                    startTime = System.currentTimeMillis()
                }
                
                else -> {
                }
            }
            false
        }
    }
    
    @SuppressLint("ClickableViewAccessibility")
    private fun initPreviousButtonEvent() {
        var startTime = 0L
        
        val previousButton = this.mainActivity.findViewById<Button>(R.id.previousButton)
        // previousButton.setOnClickListener {
        //     this.mainActivity.jumpToPreviousImage()
        // }
        previousButton.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    startTime = System.currentTimeMillis()
                    this.startAutoScrollPrevious()
                }
                
                MotionEvent.ACTION_CANCEL,
                MotionEvent.ACTION_UP,
                -> {
                    this.stopAutoScroll()
                    if (System.currentTimeMillis() - startTime < Const.AUTO_SCROLL_DELAY) {
                        this.mainActivity.jumpToPreviousImage()
                    }
                    startTime = System.currentTimeMillis()
                }
                
                else -> {
                }
            }
            false
        }
    }
}