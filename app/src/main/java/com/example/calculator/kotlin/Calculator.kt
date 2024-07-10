package com.example.calculator.kotlin

import android.icu.text.DecimalFormat
import java.util.Stack

class Calculator {
    fun evaluateExpression(expression: String): Double {
        val tokens = Prepare().normalizeExpression(expression).toCharArray()
//        val tokens = expression.toCharArray()

        val values: Stack<Double> = Stack()
        val ops: Stack<Char> = Stack()

        var i = 0
        while (i < tokens.size) {
            if (tokens[i] == ' ') {
                i++
                continue
            }

            // Handle number (including decimal numbers)
            if (tokens[i] in '0'..'9' || tokens[i] == '.') {
                val sbuf = StringBuilder()
                while (i < tokens.size && (tokens[i] in '0'..'9' || tokens[i] == '.')) sbuf.append(tokens[i++])
                values.push(sbuf.toString().toDouble())
                i--
            }
            // Handle negative number
            else if (tokens[i] == '-' && (i == 0 || tokens[i - 1] == '(' || Additionally().isOperator(tokens[i - 1]))) {
                val sbuf = StringBuilder()
                sbuf.append(tokens[i++])
                while (i < tokens.size && (tokens[i] in '0'..'9' || tokens[i] == '.')) sbuf.append(tokens[i++])
                values.push(sbuf.toString().toDouble())
                i--
            }
            // Handle opening parenthesis
            else if (tokens[i] == '(') {
                ops.push(tokens[i])
            }
            // Handle closing parenthesis
            else if (tokens[i] == ')') {
                while (ops.peek() != '(') values.push(applyOp(ops.pop(), values.pop(), values.pop()))
                ops.pop()
            }
            // Handle operator
            else if (Additionally().isOperator(tokens[i])) {
                while (ops.isNotEmpty() && hasPrecedence(tokens[i], ops.peek())) values.push(applyOp(ops.pop(), values.pop(), values.pop()))
                ops.push(tokens[i])
            }
            i++
        }

        while (ops.isNotEmpty()) values.push(applyOp(ops.pop(), values.pop(), values.pop()))

        return Additionally().formatResult(values.pop()).toDouble()
    }

    private fun applyOp(op: Char, b: Double, a: Double): Double {
        return when (op) {
            '+' -> a + b
            '-' -> a - b
            '×' -> a * b
            '÷' -> {
                if (b == 0.0) return Double.POSITIVE_INFINITY // Handle division by zero
//                if (b == 0.0) throw UnsupportedOperationException("Cannot divide by zero")
//                хто таке сказав що так не можна робить
                a / b
            }
            else -> throw UnsupportedOperationException("Unsupported operator $op")
        }
    }

    private fun hasPrecedence(op1: Char, op2: Char): Boolean {
        if (op2 == '(' || op2 == ')') return false
        return (op1 != '×' && op1 != '÷') || (op2 != '+' && op2 != '-')
    }
}


class Prepare {
     internal fun normalizeExpression(expression: String): String {
         var normalizedExpression = StringBuilder(expression)
         normalizedExpression = normalizeDots(normalizedExpression)
         normalizedExpression = normalizeParentheses(normalizedExpression)
         normalizedExpression = normalizeOperators(normalizedExpression)

         return normalizedExpression.toString()
    }

    private fun normalizePi(expression: String): String {
        return expression.replace("π", Math.PI.toString())
    }


    private fun normalizeDots(expression: StringBuilder): StringBuilder {
        var i = 0
        while (i < expression.length) {
            val currentChar = expression[i]
            val previousChar = if (i > 0) expression[i - 1] else null
            val nextChar = if (i < expression.length - 1) expression[i + 1] else null

            if (currentChar == '.') {
                if ((previousChar == null || !previousChar.isDigit()) && (nextChar == null || !nextChar.isDigit())) {
                    // Remove dot if it's not surrounded by digits
                    expression.deleteCharAt(i)
                    continue // Skip to the next character after deletion
                }
                if (previousChar == null || !previousChar.isDigit()) {
                    // Insert '0' before the dot
                    expression.insert(i, '0')
                    i++ // Skip the newly inserted '0'
                }
                if (nextChar == null || !nextChar.isDigit()) {
                    // Insert '0' after the dot
                    expression.insert(i + 1, '0')
                    i++ // Skip the newly inserted '0'
                }
            }
            i++
        }
        return expression
    }

    private fun normalizeParentheses(expression: StringBuilder): StringBuilder {
        val openCount = expression.count { it == '(' }
        var closeCount = expression.count { it == ')' }

        while (openCount > closeCount) {
            expression.append(')')
            closeCount++
        }

        while (openCount < closeCount) {
            val lastCloseIndex = expression.lastIndexOf(')')
            if (lastCloseIndex != -1) {
                expression.deleteCharAt(lastCloseIndex)
                closeCount--
            } else {
                break
            }
        }
        return expression
    }

    private fun normalizeOperators(expression: StringBuilder): StringBuilder {
        val normalizedOperators = StringBuilder(expression)

        // First pass: Remove invalid operators
        val toRemove = mutableListOf<Int>()
        for (i in normalizedOperators.indices) {
            val currentChar = normalizedOperators[i]
            val previousChar = if (i > 0) normalizedOperators[i - 1] else null
            val nextChar = if (i < normalizedOperators.length - 1) normalizedOperators[i + 1] else null

            // Mark operator for removal if it's right after another operator or '('
            if ((previousChar != null && (Additionally().isOperator(previousChar) || previousChar == '(')) && Additionally().isOperator(currentChar)) {
                toRemove.add(i)
            }

            // Mark operator for removal if it's right before a ')'
            if (nextChar == ')' && Additionally().isOperator(currentChar)) {
                toRemove.add(i)
            }

            // Remove operator if nothing is before it and it's followed by an operator
            if ((previousChar == null || nextChar == null) && Additionally().isOperator(currentChar)) {
                toRemove.add(i)
            }

            // Check if '-' is before a number or '('
            if (currentChar == '-' && (previousChar == null || previousChar == '(' || Additionally().isOperator(previousChar))) {
                toRemove.remove(i) // Ensure '-' is not marked for removal
            }
        }

        // Remove marked characters
        for (index in toRemove.asReversed()) {
            normalizedOperators.deleteCharAt(index)
        }

        // Second pass: Add multiplication operators where necessary
        val toAdd = mutableListOf<Pair<Int, String>>()
        for (i in normalizedOperators.indices) {
            val currentChar = normalizedOperators[i]
            val previousChar = if (i > 0) normalizedOperators[i - 1] else null
            val nextChar = if (i < normalizedOperators.length - 1) normalizedOperators[i + 1] else null

            if (currentChar == 'π' || currentChar == '(' || currentChar == ')') {
                if (previousChar != null && previousChar != '(' && !Additionally().isOperator(previousChar)) {
                    toAdd.add(Pair(i, "×"))
                }

                if (currentChar == 'π') {
                    toAdd.add(Pair(i + 1, Math.PI.toString()))
                }

                if (nextChar != null && nextChar != ')' && !Additionally().isOperator(nextChar)) {
                    toAdd.add(Pair(i + 1, "×"))
                }
            }
        }

        // Add necessary characters
        var offset = 0
        for ((index, value) in toAdd) {
            normalizedOperators.insert(index + offset, value)
            offset += value.length
        }

        return normalizedOperators
    }
}

class Additionally {
    fun formatResult(result: Double): String {
        return DecimalFormat("#.##").format(result).toString()
    }

    internal fun isOperator(char: Char?): Boolean {
        return char == '+' || char == '-' || char == '×' || char == '÷'
    }
}
