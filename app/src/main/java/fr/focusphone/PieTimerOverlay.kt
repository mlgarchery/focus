package fr.focusphone

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager

object PieTimerOverlay {

    private var pieTimerView: PieTimerView? = null
    private val handler = Handler(Looper.getMainLooper())
    private var hideRunnable: Runnable? = null

    fun show(context: Context, durationMs: Long) {
        if (pieTimerView != null) {
            // If already showing, just restart the countdown
            pieTimerView?.startCountdown(durationMs)
            return
        }

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        pieTimerView = PieTimerView(context)

        val size = (40  * context.resources.displayMetrics.density).toInt() // 80dp in pixels

        val params = WindowManager.LayoutParams(
            size,
            size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 16 // 16px margin from left
            y = 16 // 16px margin from top
        }

        wm.addView(pieTimerView, params)
        pieTimerView?.startCountdown(durationMs)

        // Schedule auto-hide after duration
        hideRunnable = Runnable {
            hide(context)
        }
        handler.postDelayed(hideRunnable!!, durationMs)
    }

    fun hide(context: Context) {
        // Cancel any pending hide
        hideRunnable?.let {
            handler.removeCallbacks(it)
            hideRunnable = null
        }

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        pieTimerView?.let { 
            it.stopCountdown()
            wm.removeView(it) 
        }
        pieTimerView = null
    }
}
