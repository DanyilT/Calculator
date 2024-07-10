package com.example.calculator.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.calculator.R
import com.example.calculator.java.AutoResizeEditText
import com.example.calculator.kotlin.Calculator

class MainActivity : AppCompatActivity() {
    private lateinit var display: AutoResizeEditText // Custom EditText with autoSizeText
    private lateinit var preResult: TextView
    private lateinit var buttonClear: Button
    private lateinit var buttonAC: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        display = findViewById(R.id.display)
        preResult = findViewById(R.id.pre_result)
        buttonClear = findViewById(R.id.buttonClear)
        buttonAC = findViewById(R.id.buttonAC)

        display.showSoftInputOnFocus = false
        display.setMinTextSize(24f)
        display.setMaxTextSize(49f)

        setButtonListeners()
        setupLongPressHandler()
    }

    private fun setupLongPressHandler() {
        LongPressHandler(
            context = this,
            buttonClear = buttonClear,
            buttonAC = buttonAC,
            onAction = { appendToExpression("all", "clear") }
        )
    }

    private fun setButtonListeners() {
        val buttons = listOf(
            R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4,
            R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9,
            R.id.buttonAdd, R.id.buttonSubtract, R.id.buttonMultiply, R.id.buttonDivide,
            R.id.buttonDot, R.id.buttonEquals, R.id.buttonClear, R.id.buttonParentheses,
            R.id.buttonPi, R.id.buttonAC
        )

        for (id in buttons) {
            findViewById<Button>(id).setOnClickListener(buttonClickListener)
        }
    }

    private val buttonClickListener = View.OnClickListener { view ->
        var showPreResult = true;
        when (view.id) {
            R.id.button0 -> appendToExpression("0", "number")
            R.id.button1 -> appendToExpression("1", "number")
            R.id.button2 -> appendToExpression("2", "number")
            R.id.button3 -> appendToExpression("3", "number")
            R.id.button4 -> appendToExpression("4", "number")
            R.id.button5 -> appendToExpression("5", "number")
            R.id.button6 -> appendToExpression("6", "number")
            R.id.button7 -> appendToExpression("7", "number")
            R.id.button8 -> appendToExpression("8", "number")
            R.id.button9 -> appendToExpression("9", "number")
            R.id.buttonAdd -> appendToExpression("+", "operation")
            R.id.buttonSubtract -> appendToExpression("-", "operation")
            R.id.buttonMultiply -> appendToExpression("×", "operation")
            R.id.buttonDivide -> appendToExpression("÷", "operation")
            R.id.buttonDot -> appendToExpression(".", "dot")
            R.id.buttonParentheses -> appendToExpression("()", "parentheses")
            R.id.buttonPi -> appendToExpression("π", "pi")
            R.id.buttonClear -> {
                appendToExpression("1", "clear")
            }
            R.id.buttonAC -> {
                appendToExpression("all", "clear")
                showPreResult = false
            } R.id.buttonEquals -> {
                display.setText(calculateResult(display.text.toString()))
                display.setSelection(display.text?.length ?: 0)
                showPreResult = false
            }
        }
        val regex = "[+\\-×÷π]".toRegex()
        if (regex.containsMatchIn(display.text.toString()) && showPreResult && display.text.toString() != calculateResult(display.text.toString())) preResult.text = calculateResult(display.text.toString()) else preResult.text = ""
    }

    private fun appendToExpression(value: String, type: String) {
        var itIsShowingOnTheTextViewDisplay = display.text.toString()
        var cursorPosition = display.selectionStart
        val previousChar = if (cursorPosition > 0) itIsShowingOnTheTextViewDisplay[cursorPosition - 1] else null
        val nextChar = if (cursorPosition < itIsShowingOnTheTextViewDisplay.length) itIsShowingOnTheTextViewDisplay[cursorPosition] else null

        when (type) {
            "number" -> {
                itIsShowingOnTheTextViewDisplay = insertStringAtPosition(itIsShowingOnTheTextViewDisplay, value, cursorPosition)
                cursorPosition += value.length
            }

            "dot" -> {
                if (previousChar?.isDigit() == true && !hasDotInCurrentNumber(cursorPosition, itIsShowingOnTheTextViewDisplay)) {
                    itIsShowingOnTheTextViewDisplay = insertStringAtPosition(itIsShowingOnTheTextViewDisplay, value, cursorPosition)
                    cursorPosition += value.length
                } else if ((nextChar?.isDigit() == true && !hasDotInCurrentNumber(cursorPosition, itIsShowingOnTheTextViewDisplay)) || itIsShowingOnTheTextViewDisplay.isEmpty()) {
                    val insertValue = "0$value"
                    itIsShowingOnTheTextViewDisplay = insertStringAtPosition(itIsShowingOnTheTextViewDisplay, insertValue, cursorPosition)
                    cursorPosition += insertValue.length
                }
            }

            "operation" -> {
                val regex = "[+\\-×÷]".toRegex()
                if (!regex.matches(previousChar.toString())) {
                    itIsShowingOnTheTextViewDisplay = insertStringAtPosition(itIsShowingOnTheTextViewDisplay, value, cursorPosition)
                    cursorPosition += value.length
                } else if (previousChar == '.') {
                    val insertValue = "0$value"
                    itIsShowingOnTheTextViewDisplay = insertStringAtPosition(itIsShowingOnTheTextViewDisplay, insertValue, cursorPosition)
                    cursorPosition += insertValue.length
                }
            }

            "parentheses" -> {
                val insertValue = if (previousChar == '.') "0$value" else if (nextChar == '.') "${value}0" else value
                itIsShowingOnTheTextViewDisplay = insertStringAtPosition(itIsShowingOnTheTextViewDisplay, insertValue, cursorPosition)
                cursorPosition += insertValue.length -1
            }

            "pi" -> {
                val insertValue = if (previousChar == '.') "0$value" else if (nextChar == '.') "${value}0" else value
                itIsShowingOnTheTextViewDisplay = insertStringAtPosition(itIsShowingOnTheTextViewDisplay, insertValue, cursorPosition)
                cursorPosition += insertValue.length
            }

            "clear" -> {
                if (value == "all") {
                    itIsShowingOnTheTextViewDisplay = ""
                    cursorPosition = 0
                    preResult.text = ""
//                    showPreResult = false
                } else if (cursorPosition > 0) {
                    if (previousChar == '(' && nextChar == ')') {
                        itIsShowingOnTheTextViewDisplay = itIsShowingOnTheTextViewDisplay.removeRange(cursorPosition - value.toInt(), cursorPosition + value.toInt())
                    } else {
                        itIsShowingOnTheTextViewDisplay = itIsShowingOnTheTextViewDisplay.removeRange(cursorPosition - value.toInt(), cursorPosition)
                    }
                    // value.toInt() == 1
                    cursorPosition -= value.toInt()
                }
            }
        }
        display.setText(itIsShowingOnTheTextViewDisplay)
        display.setSelection(cursorPosition)
    }

    private fun hasDotInCurrentNumber(cursorPosition: Int, expression: String): Boolean {
        val leftSide = expression.substring(0, cursorPosition).takeLastWhile { it.isDigit() || it == '.' }
        val rightSide = expression.substring(cursorPosition).takeWhile { it.isDigit() || it == '.' }
        return leftSide.contains('.') || rightSide.contains('.')
    }

    private fun insertStringAtPosition(original: String, insert: String, position: Int): String {
        return original.substring(0, position) + insert + original.substring(position)
    }

    private fun calculateResult(itIsShowingOnTheTextViewDisplay: String): String {
        if (itIsShowingOnTheTextViewDisplay.isNotEmpty()) {
            try {
                return Calculator().evaluateExpression(itIsShowingOnTheTextViewDisplay).toString()
            } catch (e: Exception) {
                 return "idk?"
            }
        } else {
            return itIsShowingOnTheTextViewDisplay
        }
    }
}
