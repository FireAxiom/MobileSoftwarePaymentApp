package com.example.myapplication.ui.profile

import android.annotation.SuppressLint
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.data.AppViewModel
import com.example.myapplication.ui.components.AppScreen
import com.example.myapplication.ui.components.ClickableRow
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SectionTitle
import com.example.myapplication.ui.components.SwitchRow

private val currencies = listOf("USD", "EUR", "DKK")
private val languages = listOf("English", "Dansk")
private val dateFormats = listOf("DD/MM/YYYY", "MM/DD/YYYY", "YYYY-MM-DD")

/** Picks the next item in the list (tap a row to cycle through the options). */
private fun List<String>.after(current: String): String = this[(indexOf(current) + 1) % size]

/** 26 App settings – preferences. Save → 25 Profile. */
@Composable
fun AppSettingsScreen(
    vm: AppViewModel,
    onBack: () -> Unit,
    onNotificationSettings: () -> Unit,
) {
    var currency by rememberSaveable { mutableStateOf(vm.settings.currency) }
    var language by rememberSaveable { mutableStateOf(vm.settings.language) }
    var dateFormat by rememberSaveable { mutableStateOf(vm.settings.dateFormat) }

    AppScreen(title = "App settings", onBack = onBack) {
        SectionTitle("Preferences")
        ClickableRow("Default currency", "Used for newly created groups", currency) { currency = currencies.after(currency) }
        ClickableRow("Language", trailing = language) { language = languages.after(language) }
        ClickableRow("Date format", trailing = dateFormat) { dateFormat = dateFormats.after(dateFormat) }
        ClickableRow("Notification settings", "Push and email preferences", "›", onClick = onNotificationSettings)

        SectionTitle("Developer")
        SwitchRow(
            title = "Simulate offline mode",
            checked = !vm.isOnline,
            subtitle = "Shows the offline banners. Turning it off adds a \"Sync succeeded\" notification.",
        ) { offline -> vm.setConnection(online = !offline) }

        SectionTitle("About")
        ClickableRow("Help & support", trailing = "›")
        ClickableRow("Terms & privacy", trailing = "›")
        Text("Version 1.0", style = MaterialTheme.typography.bodySmall)

        PrimaryButton("Save preferences") {
            vm.settings = vm.settings.copy(currency = currency, language = language, dateFormat = dateFormat)
            onBack()
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun AppSettingsScreenPreview() {
    AppSettingsScreen(
        vm = AppViewModel(),
        onBack = {},
        onNotificationSettings = {},
    )
}