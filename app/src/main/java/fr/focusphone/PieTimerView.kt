package fr.focusphone

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class PieTimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint().apply {
        color = 0x80000000.toInt() // Semi-transparent black
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val piePaint = Paint().apply {
        color = 0xFFFF5252.toInt() // Red color
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val borderPaint = Paint().apply {
        color = 0xFFFFFFFF.toInt() // White border
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private var sweepAngle = 360f
    private val rectF = RectF()
    private var animator: ValueAnimator? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = Math.min(width, height).toFloat()
        val padding = 8f
        rectF.set(padding, padding, size - padding, size - padding)

        // Draw background circle
        canvas.drawCircle(size / 2, size / 2, (size - padding * 2) / 2, backgroundPaint)

        // Draw pie (unfilling from 360 to 0)
        if (sweepAngle > 0) {
            canvas.drawArc(rectF, -90f, sweepAngle, true, piePaint)
        }

        // Draw border
        canvas.drawCircle(size / 2, size / 2, (size - padding * 2) / 2, borderPaint)
    }

    fun startCountdown(durationMs: Long) {
        animator?.cancel()
        
        animator = ValueAnimator.ofFloat(360f, 0f).apply {
            duration = durationMs
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                sweepAngle = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun stopCountdown() {
        animator?.cancel()
        sweepAngle = 360f
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}
