package com.example.myapplication.ui.auth

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.AppScreen
import com.example.myapplication.ui.components.Placeholder
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SecondaryButton

/** 01 Start page – choose Log in or Sign up. */
@Composable
fun StartScreen(onLogIn: () -> Unit, onSignUp: () -> Unit) {
    AppScreen(
        title = "",
    ) {
        Placeholder("Shared expenses illustration", 180.dp)
        Text("Share the bill.\nKeep it simple.", style = MaterialTheme.typography.headlineMedium)
        Text("Track group expenses, see who owes what, and settle up in one place.")
        PrimaryButton("Log in", onClick = onLogIn)
        SecondaryButton("Create an account", onClick = onSignUp)
    }
}

@Preview
@Composable
fun StartScreenPreview() {
    StartScreen(
        onLogIn = {},
        onSignUp = {}
    )
}
