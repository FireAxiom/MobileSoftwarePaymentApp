package com.example.myapplication.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.components.Placeholder
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SecondaryButton

/** 01 Start page – choose Log in or Sign up. */
@Composable
fun StartScreen(onLogIn: () -> Unit, onSignUp: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Placeholder("Shared expenses illustration", 180.dp)
        Text("Share the bill.\nKeep it simple.", style = MaterialTheme.typography.headlineMedium)
        Text("Track group expenses, see who owes what, and settle up in one place.")
        PrimaryButton("Log in", onClick = onLogIn)
        SecondaryButton("Create an account", onClick = onSignUp)
    }
}
