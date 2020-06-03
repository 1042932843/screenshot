package com.dusky.screenshot

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import org.greenrobot.eventbus.EventBus


class TakePhotoService : AccessibilityService() {
    override fun onCreate() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onInterrupt() {

    }

    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
        val eventType: Int = p0!!.eventType
        when (eventType) {
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {

            }
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED, AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                val className: String = p0.className.toString()

            }
        }
    }
}
