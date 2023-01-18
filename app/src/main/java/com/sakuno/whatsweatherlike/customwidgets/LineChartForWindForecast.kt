package com.sakuno.whatsweatherlike.customwidgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View

class LineChartForWindForecast @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {


    var hPadding = 0f
    var topPadding = dp2px(2f)
    var bottomPadding = 0f
    var paintStrokeWidth = dp2px(1f)

    private val linePainter = Paint()
    private var linePath = Path()

    private val lineBackgroundPainter = Paint()
    private val lineBackgroundPath = Path()

    var widgetWidth = 0f
    var widgetHeight = 0f

    private var maxPoint = 0f
    var maxData = 0f

    var lineColor = Color.BLUE
    var lineBackgroundColor = Color.BLUE

    var points = arrayOf<DataPoint>()

    var startIndex = mutableListOf<Int>()
    var endIndex = mutableListOf<Int>()

    var notZeroAtBeginning = false

    fun applyChanges() = run { invalidate() }

    private fun pointArray(): Array<PointF> {
        val resList = mutableListOf<PointF>()
        for ((i, it) in points.withIndex()) {
            resList += PointF(
                hPadding + i * (widgetWidth - hPadding * 2) / (points.size - 1),
                topPadding + (widgetHeight - topPadding - bottomPadding) * (it.denominator - it.molecule) / it.denominator
            )
        }
        return resList.toTypedArray()
    }

    data class DataPoint(
        var molecule: Float, var denominator: Float
    )

    fun setDataArray(value: DoubleArray) {
        val tempList = mutableListOf<Float>()
        value.forEach { tempList += it.toFloat() }
        setDataArray(tempList.toFloatArray())

    }

    fun setDataArray(value: FloatArray) {
        maxData = value.max()
        maxPoint = value.max().takeIf { it != 0f } ?: 1f
        val resList = mutableListOf<DataPoint>()

        var bufferItem = 0f
        startIndex.clear()
        endIndex.clear()

        for ((i, it) in value.withIndex()) {

            resList += DataPoint(it, maxPoint)
            if (i == 0) {
                notZeroAtBeginning = (it > 0f)
            } else {
                if (bufferItem == 0f && it != 0f)
                    startIndex += i - 1
                else if (bufferItem != 0f && it == 0f)
                    endIndex += i
            }
            bufferItem = it
        }



        points = resList.toTypedArray()
    }

    init {
        linePainter.apply {
            strokeWidth = paintStrokeWidth
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        lineBackgroundPainter.apply {
            alpha = 60
            strokeWidth = paintStrokeWidth
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        widgetWidth = width.toFloat()
        widgetHeight = height.toFloat()

        linePath.reset()
        lineBackgroundPath.reset()

        linePainter.color = lineColor

        lineBackgroundPainter.shader = LinearGradient(
            0f,
            topPadding,
            0f,
            widgetHeight - bottomPadding,
            lineBackgroundColor,
            Color.argb(0, 255, 255, 255),
            Shader.TileMode.MIRROR
        )

        val points = pointArray()


        Log.d("WindLineChart", "widgetWidth: $widgetWidth")
        Log.d("WindLineChart", "widgetHeight: $widgetHeight")

        for (it in points) Log.d("WindLineChart", it.toString())

        for ((i, it) in points.withIndex()) {
            if (i == 0) {
                linePath.moveTo(it.x, it.y)
                lineBackgroundPath.moveTo(it.x, it.y)
            } else {
                linePath.lineTo(it.x, it.y)
                lineBackgroundPath.lineTo(it.x, it.y)
            }
        }

        lineBackgroundPath.apply {
            lineTo(widgetWidth - hPadding, widgetHeight - bottomPadding)
            lineTo(hPadding, widgetHeight - bottomPadding)
            close()
        }

        canvas.drawPath(lineBackgroundPath, lineBackgroundPainter)
        canvas.drawPath(linePath, linePainter)
    }

    private fun dp2px(v: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics)


}