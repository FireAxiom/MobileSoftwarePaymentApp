package com.feelgoodinc.paymentapp.ui.auth

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
import com.feelgoodinc.paymentapp.data.AppViewModel
import com.feelgoodinc.paymentapp.ui.components.AppScreen
import com.feelgoodinc.paymentapp.ui.components.AppTextField
import com.feelgoodinc.paymentapp.ui.components.CheckboxRow
import com.feelgoodinc.paymentapp.ui.components.PrimaryButton
import com.feelgoodinc.paymentapp.ui.components.offlineState

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
            when {
                !vm.isOnline -> error = "You're offline. Try again when you're connected."
                password.length < 8 -> error = "Use at least 8 characters for your password."
                !vm.isAuthLoading -> {
                    error = null
                    vm.signUp(name, email, password) { signUpError ->
                        if (signUpError == null) onSignedUp() else error = signUpError
                    }
                }
            }
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