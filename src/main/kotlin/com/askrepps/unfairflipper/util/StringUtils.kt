package com.askrepps.unfairflipper.util

fun Int.centsToFormattedCurrency() = String.format("$%.2f", this / 100.0f)

fun Double.formatWithPrecision(decimalPoints: Int) = String.format("%.${decimalPoints}f", this)
