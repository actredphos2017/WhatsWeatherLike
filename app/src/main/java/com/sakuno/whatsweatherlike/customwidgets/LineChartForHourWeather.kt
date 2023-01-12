package com.sakuno.whatsweatherlike.customwidgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View

class LineChartForHourWeather @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var startPadding = dp2px(36f)
    var endPadding = dp2px(60f)

    var topPadding = dp2px(130f)
    var bottomPadding = dp2px(44f)
    var infoStart = dp2px(10f)

    var paintStrokeWidth = dp2px(1f)
    var ringEdgeWidth = dp2px(1.8f)

    private val linePainter = Paint()
    private var linePath = Path()

    private val lineBackgroundPainter = Paint()
    private val lineBackgroundPath = Path()

    private val dottedLinePainter = Paint()

    private val mipmapPainter = Paint()

    private val ringPointEdgePainter = Paint()
    private val ringPointFullPainter = Paint()

    private val textPainter = Paint()
    private val longTextPainter = Paint()
    private val timeTextPainter = Paint()

    var widgetWidth = 0f
    var widgetHeight = 0f

    var systemNightMode = false

    var day_mode_lineColor = Color.BLUE
    var day_mode_lineBackgroundColor = Color.BLUE
    var day_mode_textColor = Color.BLACK
    var day_mode_ringCenterColor = Color.WHITE

    var night_mode_lineColor = Color.BLUE
    var night_mode_lineBackgroundColor = Color.BLUE
    var night_mode_textColor = Color.WHITE
    var night_mode_ringCenterColor = Color.WHITE

    var maxTemperature: Float = 0f
    var minTemperature: Float = 0f
    var linePoints = arrayOf<Float>()

    var weatherInfoResourceArray = arrayOf<ResourceInfo>()
        set(value) {
            field = value

            val pointList = mutableListOf<Float>()
            for ((i, it) in value.withIndex()) {
                pointList += it.temperature
                if (i == 0) {
                    maxTemperature = it.temperature
                    minTemperature = it.temperature
                } else {
                    if (it.temperature > maxTemperature) maxTemperature = it.temperature
                    if (it.temperature < minTemperature) minTemperature = it.temperature
                }
            }
            linePoints = pointList.toTypedArray()
        }

    data class ResourceInfo(
        val hourTime: String,
        val temperature: Float,
        val iconBitmap: Bitmap,
        val skyCondition: String,
    )

    private fun pointArray(): Array<PointF> {
        val resList = mutableListOf<PointF>()
        for ((i, it) in linePoints.withIndex()) {
            resList += PointF(
                startPadding + i * (widgetWidth - startPadding - endPadding) / (linePoints.size - 1),
                topPadding + (widgetHeight - topPadding - bottomPadding) * (maxTemperature - it) / (maxTemperature - minTemperature)
            )
        }
        return resList.toTypedArray()
    }

    fun applyChanges() {
        invalidate()
    }

    init {
        linePainter.apply {
            strokeWidth = paintStrokeWidth
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        lineBackgroundPainter.apply {
            alpha = 60
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        dottedLinePainter.apply {
            strokeWidth = paintStrokeWidth
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(dp2px(6f), dp2px(4f)), 0f)
            isAntiAlias = true
        }

        textPainter.apply {
            textSize = dp2px(16f)
            isAntiAlias = true
        }

        longTextPainter.apply {
            textSize = dp2px(12f)
            isAntiAlias = true
        }

        timeTextPainter.apply {
            textSize = dp2px(14f)
            isAntiAlias = true
        }

        ringPointEdgePainter.apply {
            style = Paint.Style.STROKE
            strokeWidth = ringEdgeWidth
            isAntiAlias = true
        }

        ringPointFullPainter.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        widgetWidth = width.toFloat()
        widgetHeight = height.toFloat()

        if (systemNightMode) {
            linePainter.color = night_mode_lineColor
            ringPointEdgePainter.color = night_mode_lineColor
            dottedLinePainter.color = night_mode_lineColor
            textPainter.color = night_mode_textColor
            longTextPainter.color = night_mode_textColor
            timeTextPainter.color = night_mode_textColor
            ringPointFullPainter.color = night_mode_ringCenterColor
        } else {
            linePainter.color = day_mode_lineColor
            ringPointEdgePainter.color = day_mode_lineColor
            dottedLinePainter.color = day_mode_lineColor
            textPainter.color = day_mode_textColor
            longTextPainter.color = day_mode_textColor
            timeTextPainter.color = day_mode_textColor
            ringPointFullPainter.color = day_mode_ringCenterColor
        }

        linePath.reset()
        lineBackgroundPath.reset()


        val points = pointArray()

        var minY: Float = widgetHeight - bottomPadding
        for (it in points) if (it.y < minY) minY = it.y
        lineBackgroundPainter.shader = LinearGradient(
            0f,
            topPadding,
            0f,
            widgetHeight - bottomPadding,
            day_mode_lineBackgroundColor.takeIf { systemNightMode }
                ?: night_mode_lineBackgroundColor,
            Color.argb(0, 255, 255, 255),
            Shader.TileMode.MIRROR)

        Log.d("LineChart", "widgetWidth: $widgetWidth")
        Log.d("LineChart", "widgetHeight: $widgetHeight")

        for (it in points) Log.d("LineChart", it.toString())

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
            lineTo(widgetWidth - startPadding, widgetHeight - bottomPadding)
            lineTo(startPadding, widgetHeight - bottomPadding)
            close()
        }

        canvas.drawPath(lineBackgroundPath, lineBackgroundPainter)
        canvas.drawPath(linePath, linePainter)

        for ((i, it) in points.withIndex()) {

            canvas.drawLine(it.x, infoStart + dp2px(16f), it.x, it.y, dottedLinePainter)

            canvas.drawCircle(it.x, it.y, dp2px(3f), ringPointFullPainter)
            canvas.drawCircle(it.x, it.y, dp2px(3f), ringPointEdgePainter)

            canvas.drawCircle(it.x, infoStart + dp2px(16f), dp2px(3f), ringPointFullPainter)
            canvas.drawCircle(it.x, infoStart + dp2px(16f), dp2px(3f), ringPointEdgePainter)

            canvas.drawBitmap(
                weatherInfoResourceArray[i].iconBitmap,
                null,
                RectF(it.x + dp2px(6f), infoStart, it.x + dp2px(38f), infoStart + dp2px(32f)),
                mipmapPainter
            )

            canvas.drawText(weatherInfoResourceArray[i].skyCondition,
                it.x + dp2px(6f),
                infoStart + dp2px(54f),
                textPainter.takeIf { weatherInfoResourceArray[i].skyCondition.length < 3 }
                    ?: longTextPainter)

            val temperatureTextPath = Path()
            temperatureTextPath.moveTo(
                it.x,
                it.y,
            )
            temperatureTextPath.lineTo(
                it.x + dp2px(40f),
                it.y - dp2px(10f),
            )

            canvas.drawTextOnPath(
                "${weatherInfoResourceArray[i].temperature.toInt()}℃",
                temperatureTextPath,
                dp2px(10f),
                dp2px(-4f),
                timeTextPainter
            )

            canvas.drawText(
                weatherInfoResourceArray[i].hourTime,
                it.x + dp2px(-17f),
                it.y + dp2px(20f),
                timeTextPainter
            )

        }

    }

    private fun dp2px(v: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics
    )


}