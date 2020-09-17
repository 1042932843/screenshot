package com.dusky.screenshot.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.graphics.Path
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import com.dusky.screenshot.ShooterEvent
import com.dusky.screenshot.ShooterEvent.Companion.EventServiceStartFind
import com.dusky.screenshot.helper.AccessibilityHelper
import com.dusky.screenshot.helper.AccessibilityNodeInfoHelper
import com.dusky.screenshot.helper.HomeWorkHelper.FrameLayout
import com.dusky.screenshot.helper.HomeWorkHelper.PicActivity
import com.dusky.screenshot.helper.HomeWorkHelper.TextView
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
                //清空
                info0=null
                info1=null
                info2=null
            }
        }
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
        val eventType: Int = p0!!.eventType

        //获取包名
        val packages: String = p0.packageName.toString()
        if("com.baidu.homework" != packages){
            return
        }
        when (eventType) {
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {

            }
            AccessibilityEvent.TYPE_VIEW_SCROLLED->{

            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                val className: String = p0.className.toString()
                Log.d("TakePhotoService", "TYPE_WINDOW_STATE_CHANGED:$className")

                when(className){
                    PicActivity->{
                        Log.d("TakePhotoService", "AccessibilityEvent->className:$className 作业帮页面")
                        if(this.event?.event_todo!= ShooterEvent.EventTakePhoto){
                            val handler=Handler()
                            handler.postDelayed(
                                {
                                    val info=windows
                                    info.forEach {
                                        if(it.title.toString()=="作业帮"){
                                            getRecordNodeHeader(it.root) }
                                    }

                                },
                                5000//延时确保加载完毕
                            )

                        }
                    }
                    FrameLayout->{

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

    @RequiresApi(Build.VERSION_CODES.N)
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
    var info3:AccessibilityNodeInfo?=null//解答

    private fun getRecordNodeHeader(nodeInfo: AccessibilityNodeInfo) {
        if(info1!=null||info2!=null){
            return
        }
        val count = nodeInfo.childCount
        for (index in 0 until count) {
            val childNodeInfo = nodeInfo.getChild(index)
            if(info1!=null||info2!=null){
                break
            }
            if(childNodeInfo==null){
                return
            }
            getErrorPage(childNodeInfo)
            getAnSymbol(childNodeInfo)
            if(childNodeInfo.className.toString()==TextView){//排除
                return
            }
            getRecordNodeHeader(childNodeInfo)
        }

    }


    private fun getRecordNodeAnSymbol(nodeInfo: AccessibilityNodeInfo) {

        val count = nodeInfo.childCount
        for (index in 0 until count){
            val childNodeInfo = nodeInfo.getChild(index)
            getErrorPage(childNodeInfo)
            getNavHeader(childNodeInfo)
            getAnSymbol1(childNodeInfo)
            if(info2!=null){
                break
            }else if(info3!=null&&info0!=null){//保证2个都找完
                if(this.event?.event_todo==ShooterEvent.EventTakePhoto){
                    break
                }
                val rect0= AccessibilityNodeInfoHelper.getBoundsInScreen(info0)
                Log.d("TakePhotoService", "答案有帮助rect->:("+rect0.left+","+rect0.right+","+rect0.top+","+rect0.bottom+")")
                val rect1= AccessibilityNodeInfoHelper.getBoundsInScreen(info3)
                Log.d("TakePhotoService", "解答rect->:("+rect1.left+","+rect1.right+","+rect1.top+","+rect1.bottom+")")
                val y=rect1.top
                val yv=rect0.top-40
                Log.d("CorpY&YV", "y=$y,yv=$yv")
                shoot(y,yv)
                break
            }else{
                if(childNodeInfo.className.toString()==TextView){//排除
                   return
                }
                getRecordNodeAnSymbol(childNodeInfo)
            }

        }
        Log.d("TakePhotoService", "FinishLoop->className:"+nodeInfo.className+"+"+nodeInfo.text+"+"+nodeInfo.viewIdResourceName)

    }



    private fun getAnSymbol(nodeInfo: AccessibilityNodeInfo) {
        if(nodeInfo.contentDescription?.toString() == "题目解答" ||nodeInfo.text?.toString()=="题目解答") {
            if(info1==null){
                val handler=Handler()
                handler.postDelayed(
                    {
                        getRecordNodeAnSymbol(rootInActiveWindow)
                    },
                    3000//延时确保滑动完毕再查找
                )
            }
            info1=nodeInfo
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)

        }

    }

    private fun getAnSymbol1(nodeInfo: AccessibilityNodeInfo) {
        if(nodeInfo.contentDescription?.toString() == "解答" ||nodeInfo.text?.toString()=="解答") {
            info3=nodeInfo
        }
    }

    private fun getNavHeader(nodeInfo: AccessibilityNodeInfo) {
        if(nodeInfo.contentDescription?.toString()=="答案有帮助"||nodeInfo.text?.toString()=="答案有帮助"){//找到这几个字，证明webview的答案模块已经加载完毕
            Log.d("TakePhotoService", "Find->答案有帮助")
            info0=nodeInfo
        }

    }

    private fun getErrorPage(nodeInfo: AccessibilityNodeInfo){
        if(nodeInfo.contentDescription?.toString()=="没有找到答案"||nodeInfo.text?.toString()=="没有找到答案"||nodeInfo.contentDescription?.toString()=="识别未成功，再试一次吧!"||nodeInfo.text?.toString()=="识别未成功，再试一次吧!"||nodeInfo.contentDescription?.toString()=="对不起，没有找到这道题"||nodeInfo.text?.toString()=="对不起，没有找到这道题") {
            Log.d("TakePhotoService", "Find->没有找到答案")
            info2=nodeInfo
            next()
            return
        }
    }

    /**
     * 通知拍照
     */
    private fun shoot(y: Int, yv: Int) {
        if(this.event?.event_todo!= ShooterEvent.EventTakePhoto){
            val event= ShooterEvent()
            event.event_todo=
                ShooterEvent.EventTakePhoto
            event.y=y
            event.yv=yv
            EventBus.getDefault().post(event)
            this.event=event
        }
    }

    fun next(){
        val event= ShooterEvent()
        event.event_todo=ShooterEvent.EventPhotoNext
        EventBus.getDefault().post(event)
        this.event=event
    }
}
