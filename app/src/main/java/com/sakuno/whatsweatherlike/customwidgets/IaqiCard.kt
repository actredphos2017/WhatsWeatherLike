package com.sakuno.whatsweatherlike.customwidgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

class IaqiCard @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    val padding = dp2px(8f)
    val bitmapArcLength = dp2px(26f)

    val titleFontStart = dp2px(10f)
    val titleFontEnd = dp2px(22f)
    val titleFontStartPadding = dp2px(8f)
    val titleFontSize = dp2px(10f)

    val intervalBetweenTitleAndContent = dp2px(8f)

    val infoHorizontalPadding = dp2px(8f)
    val infoTopPadding = dp2px(8f)
    val infoBottomPadding = dp2px(4f)

    val indexTitleFontSize = dp2px(8f)
    val indexInfoFontSize = dp2px(16f)

    var widgetWidth: Float = 0f
    var widgetHeight: Float = 0f

    private val bitmapPainter = Paint()
    private val textPainter = Paint()
    private val splitLinePainter = Paint()
    private val indexTitlePainter = Paint()
    private val indexInfoPainter = Paint()

    var systemNightMode = false

    var day_mode_textColor = Color.BLACK
    var day_mode_main_bgColor = Color.YELLOW
    var day_mode_splitLineColor = Color.GRAY

    var night_mode_textColor = Color.WHITE
    var night_mode_main_bgColor = Color.YELLOW
    var night_mode_splitLineColor = Color.GRAY

    private val bitmapRect =
        RectF(padding, padding, padding + bitmapArcLength, padding + bitmapArcLength)

    var dataResource: IaqiDataResource? = null

    data class IaqiDataResource(
        val gasBitmap: Bitmap,
        val desc: String,
        val concentration: String,
        val iaqi: String,
        val isMain: Boolean
    )

    fun applyChanges() = invalidate()

    init {
        textPainter.apply {
            textSize = titleFontSize
            textAlign = Paint.Align.LEFT
        }

        splitLinePainter.apply {
            style = Paint.Style.STROKE
        }

        indexTitlePainter.apply {
            textSize = indexTitleFontSize
            textAlign = Paint.Align.LEFT
        }

        indexInfoPainter.apply {
            textSize = indexInfoFontSize
            textAlign = Paint.Align.LEFT
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (dataResource == null) return

        widgetWidth = width.toFloat()
        widgetHeight = height.toFloat()

        val infoStartY = padding + bitmapArcLength + intervalBetweenTitleAndContent

        if (dataResource!!.isMain)
            setBackgroundColor(if (systemNightMode) night_mode_main_bgColor else day_mode_main_bgColor)

        if (systemNightMode) {
            splitLinePainter.color = night_mode_splitLineColor
            textPainter.color = night_mode_textColor
            indexTitlePainter.color = night_mode_textColor
            indexInfoPainter.color = night_mode_textColor
        } else {
            splitLinePainter.color = day_mode_splitLineColor
            textPainter.color = day_mode_textColor
            indexTitlePainter.color = day_mode_textColor
            indexInfoPainter.color = day_mode_textColor
        }

        canvas.drawBitmap(dataResource!!.gasBitmap, null, bitmapRect, bitmapPainter)

        dataResource!!.desc.split('\n').run {
            when (size) {
                1 -> canvas.drawText(
                    this[0],
                    padding + bitmapArcLength + titleFontStartPadding,
                    padding + (titleFontStart + titleFontEnd) / 2,
                    textPainter
                )
                2 -> {
                    canvas.drawText(
                        this[0],
                        padding + bitmapArcLength + titleFontStartPadding,
                        padding + titleFontStart,
                        textPainter
                    )
                    canvas.drawText(
                        this[1],
                        padding + bitmapArcLength + titleFontStartPadding,
                        padding + titleFontEnd,
                        textPainter
                    )
                }
            }
        }

        canvas.drawLine(
            widgetWidth / 2,
            infoStartY,
            widgetWidth / 2,
            widgetHeight - padding,
            splitLinePainter
        )

        canvas.drawText(
            "浓度",
            padding + infoHorizontalPadding,
            infoStartY + infoTopPadding,
            indexTitlePainter
        )

        canvas.drawText(
            dataResource!!.concentration,
            padding + infoHorizontalPadding,
            widgetHeight - padding - infoBottomPadding,
            indexInfoPainter
        )

        canvas.drawText(
            "IAQI",
            widgetWidth / 2 + infoHorizontalPadding,
            infoStartY + infoTopPadding,
            indexTitlePainter
        )

        canvas.drawText(
            dataResource!!.iaqi,
            widgetWidth / 2 + infoHorizontalPadding,
            widgetHeight - padding - infoBottomPadding,
            indexInfoPainter
        )


    }

    private fun dp2px(v: Float): Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, v, resources.displayMetrics
    )
}