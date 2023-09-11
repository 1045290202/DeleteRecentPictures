package com.sjk.deleterecentpictures.common

import android.app.Activity
import java.util.Stack

object ActivityManager {
    
    // 活动栈
    private val activityStack: Stack<Activity> = Stack()
    
    val currentActivity: Activity?
        get() {
            return this.activityStack.lastElement()
        }
    
    /**
     * 将活动压入栈中
     */
    fun push(activity: Activity) {
        this.activityStack.push(activity)
    }
    
    /**
     * 将活动从栈中弹出
     */
    fun pop() {
        this.activityStack.pop()
    }
    
    /**
     * 将活动从栈中移除
     */
    fun remove(activity: Activity) {
        this.activityStack.remove(activity)
    }
    
    /**
     * 结束指定的活动
     */
    fun finish(activity: Activity) {
        activity.finish()
    }
    
    /**
     * 结束所有活动
     */
    fun finishAll() {
        for (activity in this.activityStack) {
            activity?.finish()
        }
    }
    
    
}