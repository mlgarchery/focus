package com.example.focus

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import org.xmlpull.v1.XmlPullParser

class ShortsAccessibilityService : AccessibilityService() {

    private var shortsStartTime: Long? = null
    private val SHORTS_LIMIT_MS = 5 * 1 * 1000L // 5 seconds!
    private val monitoredPackages: Set<String> by lazy {
        loadMonitoredPackagesFromConfig()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!monitoredPackages.contains(event.packageName?.toString())) return

        val root = rootInActiveWindow ?: return

        if (isYouTubeShorts(root)) {
            onShortsDetected()
        } else {
            onShortsExited()
        }
    }

    // TODO understand this override
    override fun onInterrupt() {}

    /**
     * Reads the packageNames from accessibility_service_config.xml
     * to avoid hardcoding them in the code.
     */
    private fun loadMonitoredPackagesFromConfig(): Set<String> {
        return try {
            val parser: XmlPullParser = resources.getXml(R.xml.accessibility_service_config)
            var eventType = parser.eventType
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && 
                    parser.name == "accessibility-service") {
                    val packageNames = parser.getAttributeValue(
                        "http://schemas.android.com/apk/res/android",
                        "packageNames"
                    )
                    if (packageNames != null) {
                        return packageNames.split(",").map { it.trim() }.toSet()
                    }
                }
                eventType = parser.next()
            }

            // Fallback if parsing fails
            emptySet()
        } catch (e: Exception) {
            e.printStackTrace()
            emptySet()
        }
    }

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

