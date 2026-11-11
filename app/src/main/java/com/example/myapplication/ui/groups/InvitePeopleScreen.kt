package com.example.myapplication.ui.groups

import android.annotation.SuppressLint
import android.content.ClipData
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.data.AppViewModel
import com.example.myapplication.ui.components.AppScreen
import com.example.myapplication.ui.components.AppTextField
import com.example.myapplication.ui.components.ClickableRow
import com.example.myapplication.ui.components.MissingScreen
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SectionTitle
import com.example.myapplication.ui.components.offlineState
import kotlinx.coroutines.launch

/** 12 Invite people – email invite, copy link, pending invitations, Done → 07. */
@Composable
fun InvitePeopleScreen(
    vm: AppViewModel,
    groupId: String,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    val group = vm.group(groupId)
    if (group == null) {
        MissingScreen(onBack)
        return
    }
    var email by rememberSaveable { mutableStateOf("") }
    var copied by remember { mutableStateOf(false) }
    var resent by remember { mutableStateOf(setOf<String>()) }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val link = "https://example.com/join/${group.id}" // placeholder link

    AppScreen(
        title = "Invite people",
        onBack = onBack,
        offline = vm.offlineState("Offline · Invite pending", "Invites are kept on this device. Will send when online."),
    ) {
        Text(group.name, style = MaterialTheme.typography.titleMedium)
        Text("Invite by email, or share a group invitation link.")
        AppTextField(email, { email = it }, "Email address", keyboardType = KeyboardType.Email)
        PrimaryButton("Send invitation", enabled = "@" in email) {
            vm.sendInvite(groupId, email)
            email = ""
        }

        SectionTitle("Group invitation link")
        ClickableRow(link, trailing = if (copied) "Copied" else "Copy") {
            scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("Invite link", link))) }
            copied = true
        }

        SectionTitle("Pending invitations")
        val pending = vm.sentInvitesFor(groupId)
        if (pending.isEmpty()) Text("No pending invitations.")
        pending.forEach { invite ->
            ClickableRow(
                title = invite.email,
                subtitle = "Pending",
                trailingContent = {
                    TextButton(onClick = { resent = resent + invite.id }) {
                        Text(if (invite.id in resent) "Sent" else "Resend")
                    }
                },
            )
        }
        PrimaryButton("Done — back to group", onClick = onDone)
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun InvitePeopleScreenPreview() {
    InvitePeopleScreen(
        vm = AppViewModel(),
        groupId = "g1",
        onBack = {},
        onDone = {},
    )
}