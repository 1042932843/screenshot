package com.dusky.screenshot

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        start.setOnClickListener {
            startActivity(Intent(this, TransparentActivity::class.java))
            this.finish()
        }
        if(enabled("TakePhotoService")){
            start.isEnabled=true
        }

    }

    private fun enabled(name: String): Boolean {
        val am =
            getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val serviceInfos =
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        val installedAccessibilityServiceList =
            am.installedAccessibilityServiceList
        for (info in installedAccessibilityServiceList) {
            Log.d("MainActivity", "all -->" + info.id)
            if (name == info.id) {
                return true
            }
        }
        return false
    }

}
