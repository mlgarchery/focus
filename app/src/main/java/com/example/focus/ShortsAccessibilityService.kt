package com.example.focus

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ShortsAccessibilityService : AccessibilityService() {

    private var shortsStartTime: Long? = null
    private val SHORTS_LIMIT_MS = 5 * 1 * 1000L // 5 seconds!

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.packageName != "com.google.android.youtube" && event.packageName != "com.vivaldi.browser" && event.packageName != "com.android.chrome") return

        val root = rootInActiveWindow ?: return

        if (isYouTubeShorts(root)) {
            onShortsDetected()
        } else {
            onShortsExited()
        }
    }

    override fun onInterrupt() {}
    private fun isYouTubeShorts(root: AccessibilityNodeInfo): Boolean {
        // Heuristic 1: Known Shorts container
        if (findNodeById(root, "com.google.android.youtube:id/reel_watch_fragment")) {
            return true
        }

        // Heuristic 2: "Shorts" tab selected
        if (findNodeWithText(root, "Shorts")) {
            return true
        }

        // Heuristic 3: Vertical pager + no seek bar
        if (looksLikeVerticalVideoFeed(root)) {
            return true
        }

        return false
    }

    private fun findNodeById(node: AccessibilityNodeInfo, id: String): Boolean {
        return node.findAccessibilityNodeInfosByViewId(id).isNotEmpty()
    }

    private fun findNodeWithText(node: AccessibilityNodeInfo, text: String): Boolean {
        if (node.text?.toString()?.contains(text, ignoreCase = true) == true) {
            return true
        }

        for (i in 0 until node.childCount) {
            node.getChild(i)?.let {
                if (findNodeWithText(it, text)) return true
            }
        }
        return false
    }

    private fun looksLikeVerticalVideoFeed(node: AccessibilityNodeInfo): Boolean {
        val className = node.className?.toString() ?: return false
        return className.contains("RecyclerView") &&
                node.childCount == 1 &&
                node.isScrollable
    }
    private fun onShortsDetected() {
        if (shortsStartTime == null) {
            shortsStartTime = System.currentTimeMillis()
        }

        val elapsed = System.currentTimeMillis() - shortsStartTime!!
        if (elapsed > SHORTS_LIMIT_MS) {
            blockShorts()
        }
    }

    private fun onShortsExited() {
        shortsStartTime = null
    }

    private fun blockShorts() {
        ShortsBlockerOverlay.show(this)
        performGlobalAction(GLOBAL_ACTION_BACK)
    }


}

