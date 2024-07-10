package com.example.calculator.activities

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.FrameLayout

class LongPressHandler(
    private val context: Context,
    private val buttonClear: Button,
    private val buttonAC: Button,
    private val onAction: () -> Unit
) : View.OnTouchListener {

    private var isButtonClearLongPressed = false

    init {
        buttonClear.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Adjust the position of buttonAC
                adjustButtonACPosition()
                buttonClear.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        buttonClear.setOnLongClickListener {
            isButtonClearLongPressed = true
            buttonAC.visibility = View.VISIBLE
            true
        }
        buttonClear.setOnTouchListener(this)
        buttonAC.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    onAction()
                    buttonAC.visibility = View.GONE
                }
            }
            false
        }
    }

    private fun adjustButtonACPosition() {
        val layoutParams = buttonAC.layoutParams as FrameLayout.LayoutParams
        val buttonClearLocation = IntArray(2)
        buttonClear.getLocationOnScreen(buttonClearLocation)
        val buttonClearTop = buttonClearLocation[1]
        val buttonClearLeft = buttonClearLocation[0]

        // Set the buttonAC position above buttonClear
        layoutParams.topMargin = buttonClearTop - 160
        layoutParams.leftMargin = buttonClearLeft
        buttonAC.layoutParams = layoutParams
    }

    override fun onTouch(view: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                if (isButtonClearLongPressed) {
                    val buttonACLocation = IntArray(2)
                    buttonAC.getLocationOnScreen(buttonACLocation)
                    val buttonACLeft = buttonACLocation[0].toFloat()
                    val buttonACTop = buttonACLocation[1].toFloat()
                    val buttonACRight = buttonACLeft + buttonAC.width
                    val buttonACBottom = buttonACTop + buttonAC.height

                    val touchX = event.rawX
                    val touchY = event.rawY

                    if (touchX >= buttonACLeft && touchX <= buttonACRight &&
                        touchY >= buttonACTop && touchY <= buttonACBottom) {
                        onAction()
                    }
                    buttonAC.visibility = View.GONE
                    isButtonClearLongPressed = false
                }
            }
        }
        return false
    }
}
