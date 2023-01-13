package com.sakuno.whatsweatherlike.customwidgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View

class LineChartForDailyWeather @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var startPadding = dp2px(48f)
    var endPadding = dp2px(48f)

    var topPadding = dp2px(110f)
    var bottomPadding = dp2px(160f)

    var infoStartPadding = dp2px(10f)
    var infoEndPadding = dp2px(20f)

    var splitLineWidth = 1.5f
    var paintStrokeWidth = dp2px(1f)
    var ringEdgeWidth = dp2px(1.8f)

    private val linePainter = Paint()
    private val lineBackgroundPainter = Paint()
    private val dottedLinePainter = Paint()
    private val mipmapPainter = Paint()
    private val ringPointEdgePainter = Paint()
    private val ringPointFullPainter = Paint()
    private val textPainter = Paint()
    private val longTextPainter = Paint()
    private val timeTextPainter = Paint()
    private val splitLinePainter = Paint()

    private val firstLinePath = Path()
    private val secondLinePath = Path()
    private val lineBackgroundPath = Path()

    private var itemInterval = 0f

    var widgetWidth = 0f
    var widgetHeight = 0f

    var systemNightMode = false

    var day_mode_lineColor = Color.BLUE
    var day_mode_lineBackgroundColor = Color.BLUE
    var day_mode_textColor = Color.BLACK
    var day_mode_ringCenterColor = Color.WHITE
    var day_mode_splitLineColor = Color.GRAY

    var night_mode_lineColor = Color.BLUE
    var night_mode_lineBackgroundColor = Color.BLUE
    var night_mode_textColor = Color.WHITE
    var night_mode_ringCenterColor = Color.WHITE
    var night_mode_splitLineColor = Color.GRAY

    var maxTemperature: Float = 0f
    var minTemperature: Float = 0f
    var temperPoints = arrayOf<Pair<Float, Float>>()

    var weatherInfoResourceArray = arrayOf<ResourceInfo>()
        set(value) {
            field = value

            val pointList = mutableListOf<Pair<Float, Float>>()
            for ((i, it) in value.withIndex()) {
                pointList += Pair(it.maxTemperature, it.minTemperature)
                if (i == 0) {
                    maxTemperature = it.maxTemperature
                    minTemperature = it.minTemperature
                } else {
                    if (it.maxTemperature > maxTemperature) maxTemperature = it.maxTemperature
                    if (it.minTemperature < minTemperature) minTemperature = it.minTemperature
                }
            }
            temperPoints = pointList.toTypedArray()
        }

    data class ResourceInfo(
        val dayOfWeek: String,
        val dayOfMonth: String,
        val skyCondition_08_20: String,
        val skyCondition_20_32: String,
        val weatherBitmap_08_20: Bitmap,
        val weatherBitmap_20_32: Bitmap,
        val maxTemperature: Float,
        val minTemperature: Float,
    )

    // first is max, second is min
    private fun initPointInfo(): Array<Pair<Float, Pair<Float, Float>>> {
        val resList = mutableListOf<Pair<Float, Pair<Float, Float>>>()

        itemInterval = (widgetWidth - startPadding - endPadding) / (temperPoints.size - 1)

        for ((i, it) in temperPoints.withIndex()) {
            resList += Pair(
                startPadding + i * itemInterval, Pair(
                    topPadding + (widgetHeight - topPadding - bottomPadding) * (maxTemperature - it.first) / (maxTemperature - minTemperature),
                    topPadding + (widgetHeight - topPadding - bottomPadding) * (maxTemperature - it.second) / (maxTemperature - minTemperature)
                )
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
            textAlign = Paint.Align.CENTER
            textSize = dp2px(16f)
            isAntiAlias = true
        }

        longTextPainter.apply {
            textAlign = Paint.Align.CENTER
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

        splitLinePainter.apply {
            strokeWidth = splitLineWidth
            style = Paint.Style.STROKE
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

        lineBackgroundPainter.shader = LinearGradient(0f,
            topPadding,
            0f,
            widgetHeight - bottomPadding,
            day_mode_lineBackgroundColor.takeIf { systemNightMode }
                ?: night_mode_lineBackgroundColor,
            Color.argb(0, 255, 255, 255),
            Shader.TileMode.MIRROR)

        splitLinePainter.shader = LinearGradient(0f,
            infoStartPadding,
            0f,
            ((widgetHeight - infoEndPadding) + infoStartPadding) / 2,
            Color.argb(0, 255, 255, 255),
            day_mode_splitLineColor.takeIf { systemNightMode } ?: night_mode_splitLineColor,
            Shader.TileMode.MIRROR)

        firstLinePath.reset()
        secondLinePath.reset()
        lineBackgroundPath.reset()

        val points = initPointInfo()

        Log.d("LineChart", "widgetWidth: $widgetWidth")
        Log.d("LineChart", "widgetHeight: $widgetHeight")

        for ((i, it) in points.withIndex()) {
            Log.d("LineChart", it.toString())
            if (i == 0) {
                firstLinePath.moveTo(it.first, it.second.first)
                secondLinePath.moveTo(it.first, it.second.second)
                lineBackgroundPath.moveTo(it.first, it.second.first)
            } else {
                firstLinePath.lineTo(it.first, it.second.first)
                secondLinePath.lineTo(it.first, it.second.second)
                lineBackgroundPath.lineTo(it.first, it.second.first)
            }

            if (i != points.size - 1) {
                canvas.drawLine(
                    it.first + itemInterval / 2,
                    infoStartPadding,
                    it.first + itemInterval / 2,
                    widgetHeight - infoEndPadding,
                    splitLinePainter
                )
            }
        }

        lineBackgroundPath.apply {
            lineTo(widgetWidth - endPadding, widgetHeight - bottomPadding)
            lineTo(startPadding, widgetHeight - bottomPadding)
            close()
        }

        canvas.drawPath(lineBackgroundPath, lineBackgroundPainter)
        canvas.drawPath(firstLinePath, linePainter)
        canvas.drawPath(secondLinePath, linePainter)

        for ((i, it) in points.withIndex()) {

            canvas.drawLine(
                it.first, it.second.first, it.first, it.second.second, dottedLinePainter
            )

            canvas.drawCircle(it.first, it.second.first, dp2px(3f), ringPointFullPainter)
            canvas.drawCircle(it.first, it.second.first, dp2px(3f), ringPointEdgePainter)

            canvas.drawText(
                "${weatherInfoResourceArray[i].maxTemperature.toInt()}°",
                it.first,
                it.second.first - dp2px(14f),
                textPainter
            )

            canvas.drawText(
                "${weatherInfoResourceArray[i].minTemperature.toInt()}°",
                it.first,
                it.second.second + dp2px(28f),
                textPainter
            )

            canvas.drawCircle(it.first, it.second.second, dp2px(3f), ringPointFullPainter)
            canvas.drawCircle(it.first, it.second.second, dp2px(3f), ringPointEdgePainter)


            canvas.drawBitmap(
                weatherInfoResourceArray[i].weatherBitmap_08_20, null, RectF(
                    it.first - dp2px(20f),
                    infoStartPadding,
                    it.first + dp2px(20f),
                    infoStartPadding + dp2px(40f)
                ), mipmapPainter
            )

            canvas.drawText(
                weatherInfoResourceArray[i].skyCondition_08_20, it.first,
                infoStartPadding + dp2px(60f), textPainter
            )


            canvas.drawBitmap(
                weatherInfoResourceArray[i].weatherBitmap_20_32, null, RectF(
                    it.first - dp2px(20f),
                    widgetHeight - infoEndPadding - dp2px(112f),
                    it.first + dp2px(20f),
                    widgetHeight - infoEndPadding - dp2px(72f)
                ), mipmapPainter
            )

            canvas.drawText(
                weatherInfoResourceArray[i].skyCondition_20_32, it.first,
                widgetHeight - infoEndPadding - dp2px(52f), textPainter
            )



            canvas.drawText(
                weatherInfoResourceArray[i].dayOfMonth,
                it.first,
                widgetHeight - infoEndPadding,
                longTextPainter
            )

            canvas.drawText(
                weatherInfoResourceArray[i].dayOfWeek,
                it.first,
                widgetHeight - infoEndPadding + dp2px(-18f),
                textPainter
            )
        }

    }

    private fun dp2px(v: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics
    )


}