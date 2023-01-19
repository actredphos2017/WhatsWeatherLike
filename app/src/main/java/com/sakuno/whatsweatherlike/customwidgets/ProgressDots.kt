package com.sakuno.whatsweatherlike.customwidgets

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

class ProgressDots @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val dotRadius = dp2px(4f)
    val currentDotRadius = dp2px(6f)
    val dotJoinRadius = dp2px(2f)
    val dotInterval = dp2px(16f)
    val limitPaddingHorizontal = dp2px(20f)

    private var goBeyond = false

    private var widgetWidth = 0f
    private var widgetHeight = 0f

    var dotNum = 1
    var currentDot = 0

    private val dotPainter = Paint()
    private val currentDotPainter = Paint()
    private val goBeyondTextPainter = Paint()
    private val satellitePath = Path()

    var dotColor = Color.GRAY
    var currentDotColor = Color.WHITE
    var textColor = Color.WHITE

    private var pointArray: Array<PointF>? = null

    fun applyChanges() = invalidate()

    private fun initPointList() {
        val startX = (widgetWidth - (dotNum - 1) * dotInterval) / 2
        goBeyond = (startX <= limitPaddingHorizontal)
        if (goBeyond) return

        val startY = widgetHeight / 2

        val resList = mutableListOf<PointF>()

        for (i in 0 until dotNum) resList += PointF(
            startX + i * dotInterval, startY
        )
        pointArray = resList.toTypedArray()
    }

    init {
        for (each in arrayOf(dotPainter, currentDotPainter)) each.apply {
            style = Paint.Style.FILL_AND_STROKE
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }

        goBeyondTextPainter.apply {
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        widgetWidth = width.toFloat()
        widgetHeight = height.toFloat()

        if (pointArray?.size != dotNum) initPointList()

        if (goBeyond) {
            goBeyondTextPainter.color = textColor
            canvas.drawText(
                "${currentDot + 1} / $dotNum",
                widgetWidth / 2,
                widgetHeight / 2,
                goBeyondTextPainter
            )
            return
        }

        dotPainter.color = dotColor
        currentDotPainter.color = currentDotColor
        dotPainter.strokeWidth = dotJoinRadius
        currentDotPainter.strokeWidth = dotJoinRadius

        satellitePath.reset()

        for ((i, it) in pointArray!!.withIndex()) {
            val dotSize = (if (i == currentDot) currentDotRadius else dotRadius) - dotJoinRadius
            val pointer = if (i == currentDot) currentDotPainter else dotPainter

            if (i == 0) {
                satellitePath.apply {
                    it.run {
                        moveTo(x - dotSize.times(1.5f), y - dotSize.times(1.5f))
                        lineTo(x + dotSize.times(1.5f), y - dotSize / 4)
                        lineTo(x, y)
                        lineTo(x - dotSize.times(1.5f) / 4, y + dotSize.times(1.5f))
                        close()
                    }
                }
                canvas.drawPath(satellitePath, pointer)
            } else {
                canvas.drawCircle(it.x, it.y, dotSize, pointer)
            }
        }

    }


    private fun dp2px(v: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics
    )
}