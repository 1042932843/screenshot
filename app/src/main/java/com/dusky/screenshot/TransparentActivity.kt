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
import com.dusky.screenshot.helper.HomeWorkHelper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.collections.ArrayList


class TransparentActivity : Activity() {
    private val REQUEST_MEDIA_PROJECTION = 0x2893
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
        when(event.event_todo){
            ShooterEvent.EventTakePhoto->{
                Log.d("TransparentActivity", "EventBus->state:requestScreenShot")
                //screenShotByShell()
                requestScreenShot()
            }
            ShooterEvent.EventPhotoNext->{
                current += 1
                postMsg()
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
                    val filePath=(Objects.requireNonNull<File>(
                        this.getExternalFilesDir("screenshot")).absoluteFile.toString()+"/"+
                            current+"/"+ SystemClock.currentThreadTimeMillis()+ ".png")
                    val dir=this.getExternalFilesDir("screenshot")?.absoluteFile.toString()+"/"+ current
                    val file = File(dir)
                    if(!file.exists()){
                        file.mkdir()
                    }
                    shooter.startScreenShot({
                        current += 1
                        Log.d("onActivityResult","shot finish")
                        postMsg()
                    },filePath)
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
                    5000
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

    fun encode(text: String): String {
        try {
            //获取md5加密对象
            val instance: MessageDigest = MessageDigest.getInstance("MD5")
            //对字符串加密，返回字节数组
            val digest:ByteArray = instance.digest(text.toByteArray())
            var sb : StringBuffer = StringBuffer()
            for (b in digest) {
                //获取低八位有效值
                var i :Int = b.toInt() and 0xff
                //将整数转化为16进制
                var hexString = Integer.toHexString(i)
                if (hexString.length < 2) {
                    //如果是一位的话，补0
                    hexString = "0" + hexString
                }
                sb.append(hexString)
            }
            return sb.toString()

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        return ""
    }


}
