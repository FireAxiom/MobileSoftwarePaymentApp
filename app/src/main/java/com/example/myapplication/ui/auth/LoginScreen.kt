package com.example.myapplication.ui.auth

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.example.myapplication.data.AppViewModel
import com.example.myapplication.ui.components.AppScreen
import com.example.myapplication.ui.components.AppTextField
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.offlineState

/** 02 Log in – existing account. Any non-empty email + password works (fake backend). */
@Composable
fun LoginScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onLoggedIn: () -> Unit,
    onSwitchToSignUp: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf(vm.currentUser.email) }
    var password by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    AppScreen(
        title = "Log in",
        onBack = onBack,
        offline = vm.offlineState("Offline · Login pending", "Not logged in yet. Will retry when online.", lastSynced = "Not yet"),
    ) {
        Text("Welcome back", style = MaterialTheme.typography.headlineSmall)
        Text("Log in to see your groups and balances.")
        AppTextField(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
        AppTextField(password, { password = it }, "Password", keyboardType = KeyboardType.Password, isPassword = true)
        // 02 · Forgot password: action exists, but there is no destination screen in the model.
        TextButton(onClick = { error = "Password reset isn't part of this version yet." }) { Text("Forgot password?") }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        PrimaryButton("Log in") {
            error = when {
                !vm.isOnline -> "You're offline. Try again when you're connected."
                !vm.logIn(email, password) -> "Enter your email and password."
                else -> null
            }
            if (error == null) onLoggedIn()
        }
        TextButton(onClick = onSwitchToSignUp, modifier = Modifier.fillMaxWidth()) { Text("New here? Sign up") }
    }
}
