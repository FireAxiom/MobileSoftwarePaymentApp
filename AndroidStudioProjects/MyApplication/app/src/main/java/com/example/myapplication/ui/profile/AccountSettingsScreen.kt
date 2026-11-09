package com.example.myapplication.ui.profile

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.example.myapplication.data.AppViewModel
import com.example.myapplication.ui.components.AppScreen
import com.example.myapplication.ui.components.AppTextField
import com.example.myapplication.ui.components.ClickableRow
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SectionTitle
import com.example.myapplication.ui.components.offlineState

/** 28 Account settings. Save → 25 Profile. Delete account requires all balances settled. */
@Composable
fun AccountSettingsScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(vm.currentUser.name) }
    var email by rememberSaveable { mutableStateOf(vm.currentUser.email) }
    var deleteError by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    AppScreen(
        title = "Account settings",
        onBack = onBack,
        offline = vm.offlineState("Offline · Changes pending", "Kept on this device. Will retry when online."),
    ) {
        AppTextField(name, { name = it }, "Full name")
        AppTextField(email, { email = it }, "Email address", keyboardType = KeyboardType.Email)
        ClickableRow("Change password", "Last updated Oct 1, 2026", "›")
        ClickableRow("Payment method", "•••• 7344", "Change")
        ClickableRow("Profile photo", trailing = "Change")
        PrimaryButton("Save account changes", enabled = name.isNotBlank() && email.isNotBlank()) {
            vm.updateAccount(name.trim(), email.trim())
            onBack()
        }

        SectionTitle("Account management")
        ClickableRow("Delete account", "Settle all balances before deleting.", "›") {
            if (vm.allSettled()) confirmDelete = true
            else deleteError = "You still have open balances. Settle all balances before deleting your account."
        }
        deleteError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete account?") },
            text = { Text("This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onAccountDeleted()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}
