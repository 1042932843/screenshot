package com.dusky.screenshot

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TransparentActivity : AppCompatActivity() {
    val REQUEST_MEDIA_PROJECTION = 1042

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //如下代码 只是想 启动一个透明的Activity 而上一个activity又不被pause
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setDimAmount(0f)

    }

    fun requestScreenShot() {
        if (Build.VERSION.SDK_INT >= 21) {
            startActivityForResult(
                createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION
            )
        } else {
            Toast.makeText(this@TransparentActivity, "仅支持5.1以上版本使用", Toast.LENGTH_LONG).show()
        }
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
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val shooter = Shooter(this, REQUEST_MEDIA_PROJECTION, data)
                    shooter.startScreenShot {
                        Log.d("onActivityResult","shot finish")
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Log.d("onActivityResult","shot cancel , Don't you forget the permission?")
                } else {
                    Log.d("onActivityResult","unknow exceptions!")

                }
            }
        }
    }
}
