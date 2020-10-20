package com.dusky.screenshot.helper

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager


object HomeWorkHelper {
    var PKG = "com.dusky.screenshot"
    var TargetPkg="com.baidu.homework"
    var PicActivity="com.baidu.homework.activity.search.PicSearchActivity"
    var webView="android.webkit.WebView"
    var View="android.view.View"
    var TextView="android.view.TextView"
    var FrameLayout="android.widget.FrameLayout"

    /**
     * 打开作业帮搜题
     */
    fun openAPP(context: Context?, path: String?) {
        try {
            val intent= Intent(Intent.ACTION_VIEW)
            intent.component= ComponentName(TargetPkg,"com.baidu.homework.activity.homework.AutoAnswerActivity")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.putExtra("INPUT_IMG_FILE",path)
            //intent.putExtra("INPUT_SEARCH_CHANNEL","zzb_yz_dsl_dsl")
            //intent.putExtra("INPUT_USE_OCR",true)
            context?.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 获取版本
     */
    fun getAppVersion(context: Context): Int {
        val pkgInfo = getPackageInfo(context) ?: return 0
        return pkgInfo.versionCode
    }

    /**
     * 获取版本名称
     */
    fun getAppVersionName(context: Context): String {
        val pkgInfo = getPackageInfo(context) ?: return ""
        return pkgInfo.versionName
    }

    /**
     * 更新包信息
     */
    private fun getPackageInfo(context: Context): PackageInfo? {
        var pkgInfo: PackageInfo? = null
        try {
            pkgInfo = context.packageManager.getPackageInfo(PKG, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return pkgInfo
    }
}