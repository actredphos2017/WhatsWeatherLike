package com.sakuno.whatsweatherlike.utils

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.sakuno.whatsweatherlike.R

class MyToast(
    val parent: Context,
    private val keepTime: Long,
    private val message: String,
    private val hasBtn: Boolean,
    private val btnTitle: String?,
    private val btnListener: ((View) -> Unit)?
) {

    private var mainDialog: Dialog = Dialog(parent, R.style.toast_dialog_full)

    init {
        mainDialog.setCancelable(true)
        mainDialog.window?.apply {
            setGravity(Gravity.BOTTOM)
            setWindowAnimations(R.style.share_animation)
            if (hasBtn) {
                View.inflate(parent, R.layout.my_toast_with_button, null).apply {
                    findViewById<TextView>(R.id.toast_message).text = message
                    findViewById<TextView>(R.id.toast_button).apply {
                        text = btnTitle
                        setOnClickListener {
                            btnListener?.apply { this(it) }
                            mainDialog.hide()
                        }
                    }
                    setContentView(this)
                }
            } else {
                View.inflate(parent, R.layout.my_toast, null).apply {
                    findViewById<TextView>(R.id.toast_message).text = message
                    setContentView(this)
                }
            }
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )

            attributes.apply {
                dimAmount = 0f
                flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            }
        }
    }

    fun show() {
        mainDialog.show()
        Thread {
            Thread.sleep(keepTime)
            mainDialog.apply {
                if (isShowing) Handler(Looper.getMainLooper()).post(::hide)
            }
        }.start()
    }

    class Builder(val parent: Context) {
        private var msg = ""
        private var hasBtn = false
        private var keepTime = 3000L
        private var btnTitle: String? = null
        private var btnListener: ((View) -> Unit)? = null

        fun create() = MyToast(parent, keepTime, msg, hasBtn, btnTitle, btnListener)

        fun setMessage(message: String): Builder {
            msg = message
            return this
        }

        fun setHoldTime(time: Long): Builder {
            keepTime = time
            return this
        }

        fun setButton(title: String, listener: (View) -> Unit): Builder {
            hasBtn = true
            btnTitle = title
            btnListener = listener
            return this
        }
    }
}