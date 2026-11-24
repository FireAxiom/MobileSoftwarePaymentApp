package com.example.myapplication.ui.notifications

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.AppViewModel
import com.example.myapplication.data.NotificationType
import com.example.myapplication.ui.components.AppScreen
import com.example.myapplication.ui.components.ClickableRow
import com.example.myapplication.ui.components.SecondaryButton
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
                NotificationType.INVITE -> OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                )
                {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp))
                    {
                        Text(n.title, style = MaterialTheme.typography.titleMedium)
                        Text(n.body)
                        SecondaryButton(text = "Review invitation", onClick = { openInviteId = n.inviteId })
                    }
                }

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