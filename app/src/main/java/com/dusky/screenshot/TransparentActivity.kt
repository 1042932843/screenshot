package com.dusky.screenshot

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.Window
import android.widget.Toast
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
    var mLocalUrl=""
    var dataList=ArrayList<String>()
    var current=0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //如下代码 只是想 启动一个透明的Activity 而上一个activity又不被pause
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setDimAmount(0f)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        val parentFile = File(getParentFile(), "Pics")

        if(parentFile.exists()){
            val children = parentFile.list()
          children.forEach {
              var path=parentFile.path+"/"+it
              dataList.add(path)
          }
        }
        postMsg()

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ShooterEvent) {
        when(event.event_todo){
            ShooterEvent.EventTakePhoto->{
                Log.d("TransparentActivity", "EventBus->state:requestScreenShot")
                //screenShotByShell()
                requestScreenShot()
            }
        }
    }



    fun requestScreenShot() {
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
                    shooter.startScreenShot {
                        current += 1
                        Log.d("onActivityResult","shot finish")
                        postMsg()

                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.d("onActivityResult","shot cancel , Don't you forget the permission?")
                } else {
                    Log.d("onActivityResult","error")

                }
            }
        }
    }


    fun postMsg(){//通知服务开始检测
        if(current<dataList.size){
            val file=File(dataList[current])
            if(file.exists()){
                HomeWorkHelper.openAPP(this,file.path)
                val handler = Handler()
                handler.postDelayed(
                    {
                        val event=ShooterEvent()
                        event.event_todo=ShooterEvent.EventServiceStartFind
                        EventBus.getDefault().post(event)
                    },
                    8000
                )
            }
        }
    }

    private fun getParentFile(): File? {
        val externalSaveDir = this.externalCacheDir
        return externalSaveDir ?: this.cacheDir
    }

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
