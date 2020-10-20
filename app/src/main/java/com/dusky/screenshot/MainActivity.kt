package com.dusky.screenshot

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dusky.screenshot.helper.AccessibilityHelper
import com.dusky.screenshot.helper.HomeWorkHelper
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity() {
    var current=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        start.setOnClickListener {
            val intent=Intent(this, TransparentActivity::class.java)
            intent.putExtra("current",current)
            startActivity(intent)
            this.finish()
        }

    }

    override fun onResume() {
        super.onResume()
        var st="OFF"
        if(AccessibilityHelper.isServiceRunning()){
            start.isEnabled=true
            st="ON"
        }else{
            start.isEnabled=false
            AccessibilityHelper.openAccessibilityServiceSettings(this)
        }
        val parentFile = File(getParentFile(), "Pics")
        var size=0
        if(parentFile.exists()){
            val children = parentFile.list()
            size=children.size
            list.text=""
            children.forEach {
                list.append(it+"\n")
            }
        }
        if(size==0){
            start.isEnabled=false
        }



        val dir=this.getExternalFilesDir("question")?.absoluteFile.toString()
        val file = File(dir)
        if(file.exists()){
            val anList=file.list()
            current = if(anList.isNotEmpty()){
                anList.size
            }else{
                0
            }

        }

        state.text= "共"+size+"个文件，预计处理时长"+(size-current)*18/60+"分\n当前处理：第$current 个\n辅助功能服务开启状态：$st(未开启无法开始)\n对象包名：com.baidu.homework\n版本："+ HomeWorkHelper.getAppVersionName(this)

    }

    private fun getParentFile(): File? {
        val externalSaveDir = this.externalCacheDir
        return externalSaveDir ?: this.cacheDir
    }
}
