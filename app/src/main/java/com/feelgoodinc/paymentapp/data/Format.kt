package com.feelgoodinc.paymentapp.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToLong

fun money(amount: Double): String = String.format(Locale.US, "$%.2f", amount)

fun round2(value: Double): Double = (value * 100).roundToLong() / 100.0

fun today(): String = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date())

fun timeNow(): String = SimpleDateFormat("h:mm a", Locale.US).format(Date())

/** "You owe $30.00" / "You are owed $64.00" / "Settled up" */
fun balanceText(net: Double): String = when {
    net < -0.005 -> "You owe ${money(abs(net))}"
    net > 0.005 -> "You are owed ${money(net)}"
    else -> "Settled up"
}

/** "Owes $30.00" / "Owed $60.00" / "Settled" */
fun netText(net: Double): String = when {
    net < -0.005 -> "Owes ${money(abs(net))}"
    net > 0.005 -> "Owed ${money(net)}"
    else -> "Settled"
}
