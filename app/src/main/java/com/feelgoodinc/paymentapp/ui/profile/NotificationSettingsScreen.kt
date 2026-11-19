package com.feelgoodinc.paymentapp.ui.profile

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.feelgoodinc.paymentapp.data.AppViewModel
import com.feelgoodinc.paymentapp.ui.components.AppScreen
import com.feelgoodinc.paymentapp.ui.components.CheckboxRow
import com.feelgoodinc.paymentapp.ui.components.PrimaryButton
import com.feelgoodinc.paymentapp.ui.components.SectionTitle
import com.feelgoodinc.paymentapp.ui.components.SwitchRow
import com.feelgoodinc.paymentapp.ui.components.offlineState

/** 27 Notification settings. Save → 26 App settings. */
@Composable
fun NotificationSettingsScreen(vm: AppViewModel, onBack: () -> Unit) {
    var draft by remember { mutableStateOf(vm.settings) }

    AppScreen(
        title = "Notification settings",
        onBack = onBack,
        offline = vm.offlineState("Offline · Preferences pending", "Preferences kept on this device. Will retry when online."),
    ) {
        SectionTitle("Delivery")
        SwitchRow("Push notifications", draft.pushNotifications) { draft = draft.copy(pushNotifications = it) }
        SwitchRow("Email notifications", draft.emailNotifications) { draft = draft.copy(emailNotifications = it) }

        SectionTitle("Notify me about")
        CheckboxRow("Group invitations", draft.notifyInvites) { draft = draft.copy(notifyInvites = it) }
        CheckboxRow("New or edited expenses", draft.notifyExpenses) { draft = draft.copy(notifyExpenses = it) }
        CheckboxRow("Payments recorded", draft.notifyPayments) { draft = draft.copy(notifyPayments = it) }

        PrimaryButton("Save notification settings") {
            vm.settings = vm.settings.copy(
                pushNotifications = draft.pushNotifications,
                emailNotifications = draft.emailNotifications,
                notifyInvites = draft.notifyInvites,
                notifyExpenses = draft.notifyExpenses,
                notifyPayments = draft.notifyPayments,
            )
            onBack()
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun NotificationSettingsScreenPreview() {
    NotificationSettingsScreen(
        vm = AppViewModel(),
        onBack = {},
    )
}