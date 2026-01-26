package com.example.focus

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager

object ShortsBlockerOverlay {

    private var view: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var autoHideRunnable: Runnable? = null
    private val AUTO_HIDE_DELAY_MS = 30_000L // 30 seconds

    fun show(context: Context) {
        if (view != null) return

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        view = LayoutInflater.from(context).inflate(R.layout.overlay_blocker, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        wm.addView(view, params)

        // Schedule auto-hide after 30 seconds
        autoHideRunnable = Runnable {
            hide(context)
        }
        handler.postDelayed(autoHideRunnable!!, AUTO_HIDE_DELAY_MS)
    }

    fun hide(context: Context) {
        // Cancel any pending auto-hide
        autoHideRunnable?.let {
            handler.removeCallbacks(it)
            autoHideRunnable = null
        }

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        view?.let { wm.removeView(it) }
        view = null
    }
}
