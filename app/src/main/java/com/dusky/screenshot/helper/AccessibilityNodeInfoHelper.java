package com.dusky.screenshot.helper;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

public class AccessibilityNodeInfoHelper {


    public static Rect getVisibleBoundsInScreen(AccessibilityNodeInfo node, int width, int height) {
        if (node == null) {
            return null;
        }
        // targeted node's bounds
        Rect nodeRect = new Rect();
        node.getBoundsInScreen(nodeRect);

        Rect displayRect = new Rect();
        displayRect.top = 0;
        displayRect.left = 0;
        displayRect.right = width;
        displayRect.bottom = height;
        boolean intersect = nodeRect.intersect(displayRect);
        return nodeRect;
    }

    public static Rect getBoundsInParent(AccessibilityNodeInfo nodeInfo) {
        Rect rect = new Rect();
        nodeInfo.getBoundsInParent(rect);
        return rect;
    }

    public static Rect getBoundsInScreen(AccessibilityNodeInfo nodeInfo) {
        Rect rect = new Rect();
        nodeInfo.getBoundsInScreen(rect);
        return rect;
    }

    public static Rect getBoundsInScreen(AccessibilityNodeInfoCompat nodeInfo) {
        Rect rect = new Rect();
        nodeInfo.getBoundsInScreen(rect);
        return rect;
    }

    public static Rect getBoundsInParent(AccessibilityNodeInfoCompat nodeInfo) {
        Rect rect = new Rect();
        nodeInfo.getBoundsInParent(rect);
        return rect;
    }
}