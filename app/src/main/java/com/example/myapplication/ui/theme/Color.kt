package com.example.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Balance colors: red = you owe, green = you are owed. Lighter shades for dark mode.
val OweRed = Color(0xFFC62828)
val OwedGreen = Color(0xFF2E7D32)
val OweRedDark = Color(0xFFEF9A9A)
val OwedGreenDark = Color(0xFFA5D6A7)

/**
 * Text color for a balance in cents: red when negative (owe), green when positive (owed),
 * the normal text color when settled. Always pair it with words ("You owe …") – never color alone.
 */
@Composable
fun balanceColor(net: Long): Color {
    val dark = isSystemInDarkTheme()
    return when {
        net < 0 -> if (dark) OweRedDark else OweRed
        net > 0 -> if (dark) OwedGreenDark else OwedGreen
        else -> Color.Unspecified
    }
}
