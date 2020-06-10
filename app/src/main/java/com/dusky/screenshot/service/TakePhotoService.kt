package com.dusky.screenshot.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.dusky.screenshot.ShooterEvent
import com.dusky.screenshot.ShooterEvent.Companion.EventServiceStartFind
import com.dusky.screenshot.helper.AccessibilityHelper
import com.dusky.screenshot.helper.HomeWorkHelper.PicActivity
import com.dusky.screenshot.helper.HomeWorkHelper.View
import com.dusky.screenshot.helper.HomeWorkHelper.webView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class TakePhotoService : AccessibilityService() {
    var event:ShooterEvent?=null

    override fun onServiceConnected() {
        super.onServiceConnected()
        AccessibilityHelper.mService = this
        Log.d("TakePhotoService", "onServiceConnected")
    }

    override fun onCreate() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
            Log.d("TakePhotoService", "EventBus:isRegistered")
        }
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        Log.d("TakePhotoService", "EventBus:unregister")
    }

    override fun onInterrupt() {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ShooterEvent) {
        when(event.event_todo){
            EventServiceStartFind->{
                this.event=event
                Log.d("TakePhotoService", "EventBus:EventServiceStartFind")
                getRecordNode(rootInActiveWindow)
            }
        }
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
        val eventType: Int = p0!!.eventType
        when (eventType) {
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {

            }
            AccessibilityEvent.TYPE_VIEW_SCROLLED->{

            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                val className: String = p0.className.toString()
                when(className){
                    PicActivity->{
                        Log.d("TakePhotoService", "AccessibilityEvent->className:$className 启动成功")
                    }
                }
            }
        }
    }



    private fun getRecordNode(nodeInfo: AccessibilityNodeInfo) {
        val count = nodeInfo.childCount
        for (index in 0 until count){
            val child = nodeInfo.getChild(index)
            getNavHeader(child)
            Log.d("TakePhotoService", "Find->className:"+child.className+"+text"+child.text+"+viewIdResourceName:"+child.viewIdResourceName+"+contentDescription:"+child.contentDescription)
            if(child.contentDescription=="解答"||child.text=="解答"){//找到这两个字，证明webview的答案模块已经加载完毕
                Log.d("TakePhotoService", "Find->解答")
                if(this.event?.event_todo!= ShooterEvent.EventTakePhoto){
                    val event= ShooterEvent()
                    event.event_todo=
                        ShooterEvent.EventTakePhoto
                    EventBus.getDefault().post(event)
                    this.event=event
                }
                return
            }else {
                getRecordNode(child)

            }

        }
        Log.d("TakePhotoService", "FinishLoop->className:"+nodeInfo.className+"+"+nodeInfo.text+"+"+nodeInfo.viewIdResourceName)

    }

    private fun getNavHeader(nodeInfo: AccessibilityNodeInfo) {
        if(nodeInfo.contentDescription=="题目解答"||nodeInfo.text=="题目解答") {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }

    }
}
