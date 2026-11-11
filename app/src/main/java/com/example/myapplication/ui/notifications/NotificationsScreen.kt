package com.example.myapplication.ui.notifications

import android.annotation.SuppressLint
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.data.AppViewModel
import com.example.myapplication.data.NotificationType
import com.example.myapplication.ui.components.AppScreen
import com.example.myapplication.ui.components.ClickableRow
import com.example.myapplication.ui.components.offlineState

/** 04 Notifications tab – invitations, activity and sync messages. */
@Composable
fun NotificationsScreen(
    vm: AppViewModel,
    onOpenGroup: (groupId: String) -> Unit,
    onOpenExpense: (expenseId: String) -> Unit,
) {
    // Which invite pop-up (10) is open. null = none. It's a sheet, not a navigation destination.
    var openInviteId by rememberSaveable { mutableStateOf<String?>(null) }

    AppScreen(
        title = "Notifications",
        offline = vm.offlineState(message = "Showing previously synced notifications."),
    ) {
        if (vm.notifications.isEmpty()) Text("You're all caught up.")
        vm.notifications.forEach { n ->
            when (n.type) {
                NotificationType.INVITE -> ClickableRow(
                    title = n.title,
                    subtitle = n.body,
                    trailingContent = { TextButton(onClick = { openInviteId = n.inviteId }) { Text("Review ›") } },
                    onClick = { openInviteId = n.inviteId },
                )

                NotificationType.ACTIVITY -> ClickableRow(
                    title = n.title,
                    subtitle = "${n.body} · ${n.time}",
                    trailing = "›",
                    onClick = {
                        if (n.expenseId != null) onOpenExpense(n.expenseId)
                        else if (n.groupId != null) onOpenGroup(n.groupId)
                    },
                )

                NotificationType.SYNC -> ClickableRow(title = n.title, subtitle = "${n.body} · ${n.time}")
            }
            HorizontalDivider()
        }
    }

    val invite = openInviteId?.let { vm.invite(it) }
    if (invite != null) {
        InviteSheet(vm, invite, onClose = { openInviteId = null })
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun NotificationsScreenPreview() {
    NotificationsScreen(
        vm = AppViewModel(),
        onOpenGroup = {},
        onOpenExpense = {},
    )
}