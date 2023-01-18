package com.sakuno.whatsweatherlike.customwidgets

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlin.math.abs


class BetterSwipeRefreshLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : SwipeRefreshLayout(context, attrs) {

    private var startX = 0f
    private var startY = 0f
    private var isDragging = false
    private var mTouchSlop: Int

    init {
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                isDragging = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging)
                    return false
                val distanceX = abs(event.x - startX)
                val distanceY = abs(event.y - startY)
                if (distanceX > mTouchSlop && distanceX > distanceY) {
                    isDragging = true
                    return false
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isDragging = false
        }

        return super.onInterceptTouchEvent(event)
    }

}