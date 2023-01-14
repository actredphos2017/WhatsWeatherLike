package com.sakuno.whatsweatherlike.customwidgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import kotlin.math.pow
import kotlin.math.sqrt

class AqiScaler @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    val topPadding = dp2px(20f)
    val bottomPadding = dp2px(54f)
    val startPadding = dp2px(12f)
    val endPadding = dp2px(32f)
    val infoEndPadding = dp2px(12f)
    val bottomCircleRadius = dp2px(18f)
    val scalerHalfWidth = dp2px(6f)
    val shadowRadius = dp2px(4f)

    val paintStrokeWidth = dp2px(0.5f)

    var systemNightMode = false

    var day_mode_bgColor = Color.WHITE
    var day_mode_shadowColor = Color.GRAY
    var day_mode_scaleTextColor = Color.GRAY

    var night_mode_bgColor = Color.GRAY
    var night_mode_shadowColor = Color.GRAY
    var night_mode_scaleTextColor = Color.WHITE

    var insideInfoTextColor = Color.WHITE

    var widgetWidth = 0f
    var widgetHeight = 0f

    var availableValue = 0f

    var scaleGroup = arrayOf<ScaleData>()

    data class ScaleData(
        val value: Float,
        val desc: String,
        val color: (Boolean) -> Int
    )

    val bgPath = Path()
    val availablePath = Path()

    val bgPainter = Paint()
    val availablePainter = Paint()
    val shadowPainter = Paint()
    val scaleTextPainter = Paint()
    val infoAqiTextPainter = Paint()
    val infoGradeTextPainter = Paint()
    val dottedPainter = Paint()

    fun applyChanges() {
        invalidate()
    }

    init {
        shadowPainter.apply {
            style = Paint.Style.FILL
            maskFilter = BlurMaskFilter(shadowRadius, BlurMaskFilter.Blur.SOLID)
            isAntiAlias = true
        }

        bgPainter.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        availablePainter.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        scaleTextPainter.apply {
            textSize = dp2px(8f)
            textAlign = Paint.Align.RIGHT
        }

        dottedPainter.apply {
            strokeWidth = paintStrokeWidth
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(dp2px(3f), dp2px(2f)), 0f)
            isAntiAlias = true
        }

        infoAqiTextPainter.apply {
            textSize = dp2px(14f)
            textAlign = Paint.Align.CENTER
        }

        infoGradeTextPainter.apply {
            textSize = dp2px(9f)
            textAlign = Paint.Align.CENTER
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        widgetWidth = width.toFloat()
        widgetHeight = height.toFloat()

        val middleWidth = (startPadding + widgetWidth - endPadding) / 2
        val targetScale: (Float) -> Float =
            { widgetHeight - bottomPadding - (widgetHeight - bottomPadding - startPadding) * it / 500f }
        val gradeIndex = getGradeIndex(availableValue)
        val circleCenterPosition =
            widgetHeight - bottomPadding + sqrt(bottomCircleRadius.pow(2) - scalerHalfWidth.pow(2))

        infoAqiTextPainter.color = insideInfoTextColor
        infoGradeTextPainter.color = insideInfoTextColor

        if (systemNightMode) {
            bgPainter.color = night_mode_bgColor
            shadowPainter.color = night_mode_shadowColor
            scaleTextPainter.color = night_mode_scaleTextColor
            dottedPainter.color = night_mode_scaleTextColor
        } else {
            bgPainter.color = day_mode_bgColor
            shadowPainter.color = day_mode_shadowColor
            scaleTextPainter.color = day_mode_scaleTextColor
            dottedPainter.color = day_mode_scaleTextColor
        }

        availablePainter.color =
            scaleGroup.getOrNull(gradeIndex)?.color?.let { it(systemNightMode) }
                ?: Color.GREEN

        bgPath.reset()

        bgPath.moveTo(middleWidth + scalerHalfWidth, widgetHeight - bottomPadding)
        availablePath.moveTo(middleWidth + scalerHalfWidth, widgetHeight - bottomPadding)

        bgPath.lineTo(middleWidth + scalerHalfWidth, topPadding)
        availablePath.lineTo(middleWidth + scalerHalfWidth, targetScale(availableValue))

        bgPath.arcTo(
            RectF(
                middleWidth - scalerHalfWidth,
                topPadding - scalerHalfWidth,
                middleWidth + scalerHalfWidth,
                topPadding + scalerHalfWidth
            ), 0f, -180f
        )

        availablePath.arcTo(
            RectF(
                middleWidth - scalerHalfWidth,
                targetScale(availableValue) - scalerHalfWidth,
                middleWidth + scalerHalfWidth,
                targetScale(availableValue) + scalerHalfWidth
            ), 0f, -180f
        )

        bgPath.lineTo(middleWidth - scalerHalfWidth, widgetHeight - bottomPadding)
        availablePath.lineTo(middleWidth - scalerHalfWidth, widgetHeight - bottomPadding)

        bgPath.close()

        for (it in scaleGroup) {
            canvas.drawLine(
                middleWidth,
                targetScale(it.value),
                widgetWidth - infoEndPadding,
                targetScale(it.value),
                dottedPainter
            )
            canvas.drawText(
                "${it.value.toInt()} ${it.desc}",
                widgetWidth - infoEndPadding,
                targetScale(it.value) - dp2px(5f),
                scaleTextPainter
            )
        }

        canvas.drawPath(bgPath, shadowPainter)

        canvas.drawCircle(
            middleWidth, circleCenterPosition, bottomCircleRadius, shadowPainter
        )

        canvas.drawPath(bgPath, bgPainter)

        canvas.drawPath(availablePath, availablePainter)

        canvas.drawCircle(
            middleWidth, circleCenterPosition, bottomCircleRadius, availablePainter
        )

        canvas.drawText(
            availableValue.toInt().toString(),
            middleWidth,
            circleCenterPosition,
            infoAqiTextPainter
        )

        canvas.drawText(
            scaleGroup.getOrNull(gradeIndex)?.desc ?: "未知",
            middleWidth,
            circleCenterPosition + dp2px(11f),
            infoGradeTextPainter
        )
    }

    private fun getGradeIndex(aqi: Float): Int {
        for ((i, it) in scaleGroup.withIndex())
            if (aqi < it.value)
                return i - 1
        return scaleGroup.size - 1
    }

    private fun dp2px(v: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics
    )
}