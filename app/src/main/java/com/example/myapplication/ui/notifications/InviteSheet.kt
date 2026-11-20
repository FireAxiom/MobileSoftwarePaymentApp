package com.example.myapplication.ui.notifications

import android.annotation.SuppressLint
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.AppViewModel
import com.example.myapplication.data.Invite
import com.example.myapplication.data.money
import com.example.myapplication.ui.components.LabelValue
import com.example.myapplication.ui.components.OfflineBanner
import com.example.myapplication.ui.components.OverlaySheet
import com.example.myapplication.ui.components.Placeholder
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SecondaryButton
import com.example.myapplication.ui.components.offlineState

/** 10 Accept or decline invite – pop-up over 04 Notifications. */
@Composable
fun InviteSheet(vm: AppViewModel, invite: Invite, onClose: () -> Unit) {
    OverlaySheet(title = "Group invitation", onDismiss = onClose) {
        vm.offlineState("Offline · Response pending", "Your response is kept on this device. Will retry when online.")
            ?.let { OfflineBanner(it) }
        Placeholder("Group cover", 100.dp)
        Text(invite.groupName, style = MaterialTheme.typography.titleLarge)
        Text("${vm.user(invite.fromUserId).name} invited you to join.")
        LabelValue("Members", invite.memberIds.joinToString { vm.firstName(it) })
        LabelValue("Your starting balance", money(0L))
        Text(invite.description, style = MaterialTheme.typography.bodySmall)
        PrimaryButton("Accept invitation") {
            vm.acceptInvite(invite.id) // the group now appears in 05 Groups
            onClose()
        }
        SecondaryButton("Decline invitation") {
            vm.declineInvite(invite.id)
            onClose()
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun InviteSheetPreview() {
    InviteSheet(
        vm = AppViewModel(),
        invite = Invite(
            id = "",
            groupName = "",
            description = "",
            fromUserId = "",
            memberIds = listOf(),
        ),
        onClose = {},
    )
}