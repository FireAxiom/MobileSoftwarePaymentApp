package com.example.myapplication.ui.auth

import android.annotation.SuppressLint
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
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.data.AppViewModel
import com.example.myapplication.ui.components.AppScreen
import com.example.myapplication.ui.components.AppTextField
import com.example.myapplication.ui.components.CheckboxRow
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.offlineState

/** 03 Sign up – create an account. */
@Composable
fun SignUpScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onSignedUp: () -> Unit,
    onSwitchToLogIn: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var agreed by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    AppScreen(
        title = "Sign up",
        onBack = onBack,
        offline = vm.offlineState("Offline · Sign-up pending", "Account not created yet. Will retry when online.", lastSynced = "Not yet"),
    ) {
        Text("Create account", style = MaterialTheme.typography.headlineSmall)
        Text("A simpler way to share expenses.")
        AppTextField(name, { name = it }, "Full name")
        AppTextField(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
        AppTextField(password, { password = it }, "Password", keyboardType = KeyboardType.Password, isPassword = true)
        Text("Use at least 8 characters.", style = MaterialTheme.typography.bodySmall)
        CheckboxRow("I agree to the Terms and Privacy Policy", agreed) { agreed = it }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        PrimaryButton("Create account", enabled = agreed) {
            error = when {
                !vm.isOnline -> "You're offline. Try again when you're connected."
                password.length < 8 -> "Use at least 8 characters for your password."
                !vm.signUp(name, email, password) -> "Fill in all fields."
                else -> null
            }
            if (error == null) onSignedUp()
        }
        TextButton(onClick = onSwitchToLogIn, modifier = Modifier.fillMaxWidth()) { Text("Already a member? Log in") }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(
        vm = AppViewModel(),
        onBack = {},
        onSignedUp = {},
        onSwitchToLogIn = {}
    )
}