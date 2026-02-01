package fr.focusphone

import android.content.Context
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView

object ShortsBlockerOverlay {

    private var view: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var autoHideRunnable: Runnable? = null
    private var timerRunnable: Runnable? = null
    private val AUTO_HIDE_DELAY_MS = 10_000L // 10 seconds
    private val TIMER_UPDATE_INTERVAL_MS = 1_000L // Update every second

    fun show(context: Context, heuristic: String = "Unknown") {
        if (view != null) return

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        view = LayoutInflater.from(context).inflate(R.layout.overlay_blocker, null)

        // Set the heuristic information
        view?.findViewById<TextView>(R.id.overlay_heuristic)?.text = 
            "Detection method: $heuristic"

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        wm.addView(view, params)

        // Start countdown timer
        startCountdownTimer(context)

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

        // Cancel timer updates
        timerRunnable?.let {
            handler.removeCallbacks(it)
            timerRunnable = null
        }

        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        view?.let { wm.removeView(it) }
        view = null
    }

    private fun startCountdownTimer(context: Context) {
        val startTime = System.currentTimeMillis()

        timerRunnable = object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - startTime
                val remaining = (AUTO_HIDE_DELAY_MS - elapsed) / 1000

                if (remaining > 0) {
                    view?.findViewById<TextView>(R.id.overlay_timer)?.text =
                        "This message will disappear in ${remaining}s"
                    handler.postDelayed(this, TIMER_UPDATE_INTERVAL_MS)
                } else {
                    view?.findViewById<TextView>(R.id.overlay_timer)?.text =
                        "Disappearing now..."
                }
            }
        }
        handler.post(timerRunnable!!)
    }
}
