package com.dusky.screenshot.helper

import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.dusky.screenshot.ShooterEvent
import org.greenrobot.eventbus.EventBus

class AccessibilityOperationFlowHelper {
    var currentEvent:ShooterEvent?=null//当前处理的事件

    companion object {

        const val TYPE_ERROR_0 = "没有找到答案"
        const val TYPE_ERROR_1 = "识别未成功，再试一次吧!"
        const val TYPE_ERROR_2 = "对不起，没有找到这道题"
        const val TYPE_ERROR_3 = "页面超时"
        const val TYPE_ERROR_4 = "答案超出屏幕"

        const val TYPE_MSG_0 = "题目处理"
        const val TYPE_MSG_1 = "解答处理"

        const val TYPE_SYMBOL_0 = "题目解答"
        const val TYPE_SYMBOL_1 = "答案有帮助"
        const val TYPE_SYMBOL_2 = "解答"
        const val TYPE_SYMBOL_3 = "题目"
        const val TYPE_SYMBOL_4 = "本题"
        const val TYPE_SYMBOL_5 = "同类题讲解"

    }

    //多条件查找
    fun findSymbolMulti(nodeInfo: AccessibilityNodeInfo,vararg strings: String): Boolean {
        var boolean=false
        run breaking@{
            strings.forEach{
                if(it==nodeInfo.contentDescription?.toString()||it==nodeInfo.text?.toString()){
                    Log.d("TakePhotoService", "FindSymbol->$it")
                    boolean=true
                    return@breaking
                }
            }
        }
        return boolean
    }

     //单条件查找
     fun findSymbol(nodeInfo: AccessibilityNodeInfo,string:String) :Boolean{
         Log.d("TakePhotoService", "FindSymbol->"+nodeInfo.contentDescription?.toString()+"/"+nodeInfo.text?.toString())
        if(nodeInfo.contentDescription?.toString()==string||nodeInfo.text?.toString()==string){
            Log.d("TakePhotoService", "FindSymbol->$string")
            return true
        }
        return false
    }


    /**
     * 通知处理
     */
    fun shoot(y: Int, yv: Int,msg: String) {
        if(currentEvent?.eventTodo!= ShooterEvent.EventTakePhoto){
            val event= ShooterEvent()
            event.eventTodo=ShooterEvent.EventTakePhoto
            event.y=y
            event.yv=yv
            event.eventMsg=msg
            EventBus.getDefault().post(event)
            currentEvent=event
        }
    }

    fun errorNext(msg:String){
        val event= ShooterEvent()
        event.eventTodo= ShooterEvent.EventErrorNext
        event.eventMsg=msg
        EventBus.getDefault().post(event)
        currentEvent=event
    }

    fun next(){
        val event= ShooterEvent()
        event.eventTodo= ShooterEvent.EventNext
        EventBus.getDefault().post(event)
        currentEvent=event
    }
}