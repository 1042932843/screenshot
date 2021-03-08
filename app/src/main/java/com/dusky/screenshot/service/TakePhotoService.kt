package com.dusky.screenshot.service

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.dusky.screenshot.ShooterEvent
import com.dusky.screenshot.helper.AccessibilityHelper
import com.dusky.screenshot.helper.AccessibilityNodeInfoHelper
import com.dusky.screenshot.helper.AccessibilityOperationFlowHelper
import com.dusky.screenshot.helper.HomeWorkHelper
import com.dusky.screenshot.helper.HomeWorkHelper.PicActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class TakePhotoService : AccessibilityService() {
    var operationFlowHelper: AccessibilityOperationFlowHelper? = null
    var currentClassName=""
    private var handler=Handler()
    private var myRootView=rootInActiveWindow

    override fun onServiceConnected() {
        super.onServiceConnected()
        AccessibilityHelper.mService = this
        operationFlowHelper = AccessibilityOperationFlowHelper()
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
        when (event.eventTodo) {
            ShooterEvent.EventServiceStartStep0-> {
                Log.d("TakePhotoService", "Step0")
                operationFlowHelper?.currentEvent = event
                yStep0=0
                yvStep0=0

            }

            ShooterEvent.EventServiceStartStep1-> {
                Log.d("TakePhotoService", "Step1")
                operationFlowHelper?.currentEvent = event
                yStep2=0
                yvStep2=0
                step1(myRootView)
            }
        }
    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
        val eventType: Int = p0!!.eventType

        //获取包名
        val packages: String = p0.packageName.toString()
        if (HomeWorkHelper.TargetPkg != packages) {
            return
        }
        when (eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED-> {
                val className: String = p0.className.toString()
                Log.d("TakePhotoService", "TYPE_WINDOW_STATE_CHANGED:$className")
                when (className) {
                    PicActivity -> {
                        currentClassName=PicActivity
                        myRootView=rootInActiveWindow
                        handler.postDelayed({
                            step0(myRootView)
                        },5000
                        )

                    }
                }
            }
        }
    }

    var yStep0=0
    var yvStep0=0

    /**
     * 处理题干
     */
    private fun step0(nodeInfo: AccessibilityNodeInfo){

        if(operationFlowHelper?.currentEvent?.eventTodo!=ShooterEvent.EventServiceStartStep0){
            return
        }
        if(currentClassName!=PicActivity){
            return
        }
        val count = nodeInfo.childCount
        for (index in 0 until count) {
            if(yStep0!=0&&yvStep0!=0){
                break
            }
            val childNodeInfo = nodeInfo.getChild(index)
            //过滤错误页面的节点
            if(operationFlowHelper!!.findSymbolMulti(childNodeInfo,AccessibilityOperationFlowHelper.TYPE_ERROR_0,AccessibilityOperationFlowHelper.TYPE_ERROR_1,AccessibilityOperationFlowHelper.TYPE_ERROR_2)){
                operationFlowHelper!!.errorNext(AccessibilityOperationFlowHelper.TYPE_ERROR_0)
                break//终止循环
            }
            //查找题目部分
            if(operationFlowHelper!!.findSymbol(childNodeInfo,AccessibilityOperationFlowHelper.TYPE_SYMBOL_3)){
                val rect0 = AccessibilityNodeInfoHelper.getBoundsInScreen(childNodeInfo)
                Log.d(
                    "TakePhotoService",
                    AccessibilityOperationFlowHelper.TYPE_SYMBOL_3+"rect->:(" + rect0.left + "," + rect0.right + "," + rect0.top + "," + rect0.bottom + ")"
                )
                yStep0=rect0.top
                if(yStep0!=0&&yvStep0!=0){
                    operationFlowHelper!!.shoot(yStep0,yvStep0,AccessibilityOperationFlowHelper.TYPE_MSG_0)
                    break
                }
            }

            //查找视频讲解（带本题）部分
            if(operationFlowHelper!!.findSymbol(childNodeInfo,AccessibilityOperationFlowHelper.TYPE_SYMBOL_4)){
                val rect0 = AccessibilityNodeInfoHelper.getBoundsInScreen(childNodeInfo)
                Log.d(
                    "TakePhotoService",
                    AccessibilityOperationFlowHelper.TYPE_SYMBOL_4+"rect->:(" + rect0.left + "," + rect0.right + "," + rect0.top + "," + rect0.bottom + ")"
                )
                if(yvStep0==0){
                    yvStep0=rect0.top
                    if(yStep0!=0&&yvStep0!=0){
                        operationFlowHelper!!.shoot(yStep0,yvStep0,AccessibilityOperationFlowHelper.TYPE_MSG_0)
                        break
                    }
                }

            }

            //查找同类型讲解部分
            if(operationFlowHelper!!.findSymbol(childNodeInfo,AccessibilityOperationFlowHelper.TYPE_SYMBOL_5)){
                val rect0 = AccessibilityNodeInfoHelper.getBoundsInScreen(childNodeInfo)
                Log.d(
                    "TakePhotoService",
                    AccessibilityOperationFlowHelper.TYPE_SYMBOL_5+"rect->:(" + rect0.left + "," + rect0.right + "," + rect0.top + "," + rect0.bottom + ")"
                )
                if(yvStep0==0){
                    yvStep0=rect0.top
                    if(yStep0!=0&&yvStep0!=0){
                        operationFlowHelper!!.shoot(yStep0,yvStep0,AccessibilityOperationFlowHelper.TYPE_MSG_0)
                        break
                    }
                }

            }

            //查找解答部分，必须放在视频讲解之后（布局造成）
            if(operationFlowHelper!!.findSymbol(childNodeInfo,AccessibilityOperationFlowHelper.TYPE_SYMBOL_2)){
                val rect0 = AccessibilityNodeInfoHelper.getBoundsInScreen(childNodeInfo)
                Log.d(
                    "TakePhotoService",
                    AccessibilityOperationFlowHelper.TYPE_SYMBOL_2+"rect->:(" + rect0.left + "," + rect0.right + "," + rect0.top + "," + rect0.bottom + ")"
                )
                if(yvStep0==0){
                    yvStep0=rect0.top
                    if(yStep0!=0&&yvStep0!=0){
                        operationFlowHelper!!.shoot(yStep0,yvStep0,AccessibilityOperationFlowHelper.TYPE_MSG_0)
                        break
                    }
                }

            }
            //递归继续
            if(childNodeInfo!=null){
                Log.d("TakePhotoService", "Parent:${childNodeInfo.className}")
                step0(childNodeInfo)
            }
        }
    }

    /**
     * 处理题目解答操作
     */
    private fun step1(nodeInfo: AccessibilityNodeInfo){
        if(operationFlowHelper?.currentEvent?.eventTodo!=ShooterEvent.EventServiceStartStep1){
            return
        }
        if(currentClassName!=PicActivity){
            return
        }
        val count = nodeInfo.childCount
        for (index in 0 until count) {
            val childNodeInfo = nodeInfo.getChild(index)
            //过滤错误页面的节点
            if(operationFlowHelper!!.findSymbolMulti(childNodeInfo,AccessibilityOperationFlowHelper.TYPE_ERROR_0,AccessibilityOperationFlowHelper.TYPE_ERROR_1,AccessibilityOperationFlowHelper.TYPE_ERROR_2)){
                operationFlowHelper!!.errorNext(AccessibilityOperationFlowHelper.TYPE_ERROR_0)
                break//终止循环
            }
            //查找题目解答部分
            if(operationFlowHelper!!.findSymbol(childNodeInfo,AccessibilityOperationFlowHelper.TYPE_SYMBOL_0)){
                childNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                val handler = Handler()
                handler.postDelayed(
                    {
                        //从头开始遍历
                        step2(myRootView)
                    },
                    3000//延时确保滑动完毕再查找
                )
                break//终止循环
            }
            //递归继续
            if(childNodeInfo!=null){
                step1(childNodeInfo)
            }
        }
    }


    var yStep2=0
    var yvStep2=0
    private fun step2(nodeInfo: AccessibilityNodeInfo){
        if(operationFlowHelper?.currentEvent?.eventTodo!=ShooterEvent.EventServiceStartStep1){
            return
        }
        if(currentClassName!=PicActivity){
            return
        }
        val count = nodeInfo.childCount
        for (index in 0 until count) {
            val childNodeInfo = nodeInfo.getChild(index)
            //过滤错误页面的节点
            if(operationFlowHelper!!.findSymbolMulti(childNodeInfo,AccessibilityOperationFlowHelper.TYPE_ERROR_0,AccessibilityOperationFlowHelper.TYPE_ERROR_1,AccessibilityOperationFlowHelper.TYPE_ERROR_2)){
                operationFlowHelper!!.errorNext(AccessibilityOperationFlowHelper.TYPE_ERROR_0)
                break//终止循环
            }
            //查找解答部分
            if(operationFlowHelper!!.findSymbol(childNodeInfo,AccessibilityOperationFlowHelper.TYPE_SYMBOL_2)){
                val rect0 = AccessibilityNodeInfoHelper.getBoundsInScreen(childNodeInfo)
                Log.d(
                    "TakePhotoService",
                    AccessibilityOperationFlowHelper.TYPE_SYMBOL_2+"rect->:(" + rect0.left + "," + rect0.right + "," + rect0.top + "," + rect0.bottom + ")"
                )
                yStep2=rect0.top
                if(yStep2!=0&&yvStep2!=0){
                    operationFlowHelper!!.shoot(yStep2,yvStep2,AccessibilityOperationFlowHelper.TYPE_MSG_1)
                    currentClassName=""//流程完毕，重置
                    break
                }
            }

            //查找答案有帮助部分
            if(operationFlowHelper!!.findSymbol(childNodeInfo,AccessibilityOperationFlowHelper.TYPE_SYMBOL_1)){
                val rect0 = AccessibilityNodeInfoHelper.getBoundsInScreen(childNodeInfo)
                Log.d(
                    "TakePhotoService",
                    AccessibilityOperationFlowHelper.TYPE_SYMBOL_1+"rect->:(" + rect0.left + "," + rect0.right + "," + rect0.top + "," + rect0.bottom + ")"
                )
                yvStep2=rect0.top-400
                if(yStep2!=0&&yvStep2!=0){
                    operationFlowHelper!!.shoot(yStep2,yvStep2,AccessibilityOperationFlowHelper.TYPE_MSG_1)
                    currentClassName=""//流程完毕，重置
                    break
                }
            }

            //递归继续
            if(childNodeInfo!=null){
                step2(childNodeInfo)
            }
        }
    }



}
