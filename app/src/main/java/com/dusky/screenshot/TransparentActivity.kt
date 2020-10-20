package com.dusky.screenshot

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.Window
import com.dusky.screenshot.helper.AccessibilityOperationFlowHelper.Companion.TYPE_ERROR_4
import com.dusky.screenshot.helper.AccessibilityOperationFlowHelper.Companion.TYPE_MSG_0
import com.dusky.screenshot.helper.AccessibilityOperationFlowHelper.Companion.TYPE_MSG_1
import com.dusky.screenshot.utils.FileUtils
import com.dusky.screenshot.helper.HomeWorkHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class TransparentActivity : Activity() {
    private val REQUEST_MEDIA_PROJECTION = 0x2893
    var dataList=ArrayList<String>()
    var errorList=ArrayList<String>()
    var current=0
    var event=ShooterEvent()
    var errorFilePath=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //如下代码 只是想 启动一个透明的Activity 而上一个activity又不被pause
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setDimAmount(0f)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        current=intent.getIntExtra("current",0)
        errorFilePath=errorFilePath()

        val parentFile = File(getParentFile(), "Pics")

        if(parentFile.exists()){
            val children = parentFile.list()
          children?.forEach {
              val path=parentFile.path+"/"+it
              dataList.add(path)
          }
        }else{
            parentFile.mkdir()
        }
        postMsg()

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ShooterEvent) {
        when(event.eventTodo){
            ShooterEvent.EventTakePhoto->{
                Log.d("TransparentActivity", "EventBus->state:requestScreenShot")
                //screenShotByShell()
                this.event=event
                requestScreenShot()
            }
            ShooterEvent.EventErrorNext->{
                error(event.eventMsg)
                current += 1
                postMsg()
            }
        }
    }



    private fun requestScreenShot() {
        startActivityForResult(
            createScreenCaptureIntent(),
            REQUEST_MEDIA_PROJECTION
        )
    }



    private fun createScreenCaptureIntent(): Intent? { //这里用media_projection代替Context.MEDIA_PROJECTION_SERVICE 可防止低于21 api编译不过
        return (getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager).createScreenCaptureIntent()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
           REQUEST_MEDIA_PROJECTION -> {
                if (resultCode == RESULT_OK && data != null) {
                    val shooter = Shooter(this, resultCode, data)
                    Log.d("onActivityResult","new Shooter")
                    var filePath=""
                    when(event.eventMsg){
                        TYPE_MSG_1->{
                            filePath=currentAnswerFilePath()
                            val fileanswer = File(filePath)
                            if(fileanswer.exists()){//存在就删tm的
                                fileanswer.delete()
                            }
                        }
                        TYPE_MSG_0->{
                            filePath=currentQuestionFilePath()
                            val fileQuestion = File(filePath)
                            if(fileQuestion.exists()){//存在就删tm的
                                fileQuestion.delete()
                            }
                        }
                    }

                    shooter.startScreenShot(object :Shooter.OnFinishedListener{
                        override fun onFinish() {
                            Log.d("onActivityResult","shot finish"+event.eventMsg)

                            when(event.eventMsg){
                                TYPE_MSG_1->{
                                    current += 1
                                    postMsg()
                                }
                                TYPE_MSG_0->{
                                    val event=ShooterEvent()
                                    event.eventTodo=ShooterEvent.EventServiceStartStep1
                                    EventBus.getDefault().post(event)
                                }
                            }

                        }

                        override fun onError() {
                            error(TYPE_ERROR_4)
                            current += 1
                            postMsg()
                        }

                    },filePath,event.y,event.yv)
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.d("onActivityResult","shot cancel , Don't you forget the permission?")
                } else {
                    Log.d("onActivityResult","error")

                }
            }
        }
    }

    fun error(type:String){
        val path=dataList[current]
        FileUtils.writeTxtToFile("$path（$type）",errorFilePath,"_log.txt")
        val file=File(path)
        if(file.exists()){
            val save=
                FileUtils.copyFile(path,errorFilePath+"/"+file.name)
            Log.d("save",save.toString())
        }
    }

    private fun currentAnswerFilePath():String{
        val dir=this.getExternalFilesDir("answer")?.absoluteFile.toString()
        val file = File(dir)
        if(!file.exists()){
            file.mkdir()
        }
        val path=dataList[current]
        val currentFile=File(path)
        return (Objects.requireNonNull<File>(getExternalFilesDir("answer")).absoluteFile.toString()+"/"+ "answer_"+ currentFile.name)
    }

    private fun currentQuestionFilePath():String{
        val dir=this.getExternalFilesDir("question")?.absoluteFile.toString()
        val file = File(dir)
        if(!file.exists()){
            file.mkdir()
        }
        val path=dataList[current]
        val currentFile=File(path)
        return (Objects.requireNonNull<File>(getExternalFilesDir("question")).absoluteFile.toString()+"/"+ "question_"+ currentFile.name)
    }

    private fun errorFilePath():String{
        val dir=this.getExternalFilesDir("error")?.absoluteFile.toString()
        val file = File(dir)
        if(!file.exists()){
            file.mkdir()
        }
        return dir
    }

    fun postMsg(){//通知服务从step0开始检测
        if(current<dataList.size){
            val file=File(dataList[current])
            if(file.exists()){
                val event=ShooterEvent()
                event.eventTodo=ShooterEvent.EventServiceStartStep0
                EventBus.getDefault().post(event)
                HomeWorkHelper.openAPP(this,file.path)
            }
        }
    }


    private fun getParentFile(): File? {
        val externalSaveDir = this.externalCacheDir
        return externalSaveDir ?: this.cacheDir
    }

    //需要root权限的拍照方式，这种更好其实
    fun screenShotByShell() {
       val mLocalUrl =
           (Objects.requireNonNull<File>(getExternalFilesDir("screenshot")).absoluteFile
                .toString() +
                    "/"
                    +
                    SystemClock.currentThreadTimeMillis() + ".png")
        val shotCmd = "screencap -p $mLocalUrl \n"
        try {
            Runtime.getRuntime().exec(shotCmd)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
