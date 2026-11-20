package com.example.myapplication.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/** Cents → "$90.00" (negative: "-$5.00"). */
fun money(cents: Long): String {
    val sign = if (cents < 0) "-" else ""
    val c = abs(cents)
    return String.format(Locale.US, "%s$%d.%02d", sign, c / 100, c % 100)
}

/** Cents → "90.00", for pre-filling input fields. */
fun plainAmount(cents: Long): String = String.format(Locale.US, "%d.%02d", cents / 100, cents % 100)

/**
 * User input → cents. Accepts "12", "12.5", "12,50". Returns null for anything else,
 * including more than 2 decimals, so the amount on screen is exactly what gets saved.
 */
fun parseCents(text: String): Long? {
    val match = Regex("""^(\d{1,7})(?:[.,](\d{0,2}))?$""").matchEntire(text.trim()) ?: return null
    val (whole, decimals) = match.destructured
    return whole.toLong() * 100 + decimals.padEnd(2, '0').toLong()
}

/** "$30.00 each", or "$33.33–$33.34 each" when the cents don't split evenly. */
fun perPersonText(amount: Long, people: Int): String {
    if (people <= 0) return ""
    val low = amount / people
    return if (amount % people == 0L) "${money(low)} each" else "${money(low)}–${money(low + 1)} each"
}

fun today(): String = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date())

fun timeNow(): String = SimpleDateFormat("h:mm a", Locale.US).format(Date())

/** "You owe $30.00" / "You are owed $64.00" / "Settled up" */
fun balanceText(net: Long): String = when {
    net < 0 -> "You owe ${money(abs(net))}"
    net > 0 -> "You are owed ${money(net)}"
    else -> "Settled up"
}

/** "Owes $30.00" / "Owed $60.00" / "Settled" */
fun netText(net: Long): String = when {
    net < 0 -> "Owes ${money(abs(net))}"
    net > 0 -> "Owed ${money(net)}"
    else -> "Settled"
}
