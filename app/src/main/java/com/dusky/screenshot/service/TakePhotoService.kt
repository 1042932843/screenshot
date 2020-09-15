package com.dusky.screenshot.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.graphics.Path
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.dusky.screenshot.ShooterEvent
import com.dusky.screenshot.ShooterEvent.Companion.EventServiceStartFind
import com.dusky.screenshot.helper.AccessibilityHelper
import com.dusky.screenshot.helper.AccessibilityNodeInfoHelper
import com.dusky.screenshot.helper.HomeWorkHelper.PicActivity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class TakePhotoService : AccessibilityService() {
    var event:ShooterEvent?=null

    override fun onServiceConnected() {
        super.onServiceConnected()
        AccessibilityHelper.mService = this
        initGesture()
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
    var path=Path();
    fun initGesture(){
        //线性的path代表手势路径,点代表按下,封闭的没用
        //x y坐标  下面例子是往下滑动界面
        path.moveTo(100f,200f);//代表从哪个点开始滑动
        path.lineTo(100f,100f);//滑动到哪个点
    }

    fun doScorll(){
        this.dispatchGesture(
            GestureDescription.Builder().addStroke(StrokeDescription(path, 20, 500)).build(),
            object : GestureResultCallback() {
                override fun onCompleted(gestureDescription: GestureDescription) {
                    super.onCompleted(gestureDescription)

                }

                override fun onCancelled(gestureDescription: GestureDescription) {
                    super.onCancelled(gestureDescription)

                }
            },
            null
        )
    }


    var info0:AccessibilityNodeInfo?=null//答案有帮助
    var info1:AccessibilityNodeInfo?=null//题目解答
    var info2:AccessibilityNodeInfo?=null//没有找到答案
    var info3:AccessibilityNodeInfo?=null

    private fun getRecordNode(nodeInfo: AccessibilityNodeInfo) {
        val count = nodeInfo.childCount
        for (index in 0 until count){
            val child = nodeInfo.getChild(index)
            getErrorPage(child)
            getNavHeader(child)
            getAnSymbol(child)

        }
        Log.d("TakePhotoService", "FinishLoop->className:"+nodeInfo.className+"+"+nodeInfo.text+"+"+nodeInfo.viewIdResourceName)

    }

    private fun getAnSymbol(nodeInfo: AccessibilityNodeInfo) {
        if(nodeInfo.contentDescription?.toString() == "题目解答" ||nodeInfo.text?.toString()=="题目解答") {
            info1=nodeInfo
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            val rect= AccessibilityNodeInfoHelper.getBoundsInScreen(nodeInfo)
            Log.d("TakePhotoService", "题目解答rect->:("+rect.left+","+rect.right+","+rect.top+","+rect.bottom+")")
        }

    }

    private fun getNavHeader(nodeInfo: AccessibilityNodeInfo) {
        if(nodeInfo.contentDescription?.toString()=="答案有帮助"||nodeInfo.text?.toString()=="答案有帮助"){//找到这几个字，证明webview的答案模块已经加载完毕
            Log.d("TakePhotoService", "Find->答案有帮助")
            info0=nodeInfo
            if(this.event?.event_todo!= ShooterEvent.EventTakePhoto){
                val event= ShooterEvent()
                event.event_todo=
                    ShooterEvent.EventTakePhoto
                EventBus.getDefault().post(event)
                this.event=event
            }
        }

    }

    private fun getErrorPage(nodeInfo: AccessibilityNodeInfo){
        if(nodeInfo.contentDescription?.toString()=="没有找到答案"||nodeInfo.text?.toString()=="没有找到答案"||nodeInfo.contentDescription=="识别未成功，再试一次吧!"||nodeInfo.text=="识别未成功，再试一次吧!"||nodeInfo.contentDescription=="对不起，没有找到这道题"||nodeInfo.text=="对不起，没有找到这道题") {
            info2=nodeInfo
            val event= ShooterEvent()
            event.event_todo=ShooterEvent.EventPhotoNext
            EventBus.getDefault().post(event)
            return
        }
    }
}
