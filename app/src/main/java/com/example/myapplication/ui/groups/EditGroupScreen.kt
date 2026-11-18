package com.example.myapplication.ui.groups

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.data.AppViewModel
import com.example.myapplication.data.ME
import com.example.myapplication.data.balanceText
import com.example.myapplication.ui.components.AppScreen
import com.example.myapplication.ui.components.AppTextField
import com.example.myapplication.ui.components.ClickableRow
import com.example.myapplication.ui.components.MissingScreen
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SecondaryButton
import com.example.myapplication.ui.components.SectionTitle
import com.example.myapplication.ui.components.offlineState
import kotlin.math.abs

/**
 * 09 Edit group – name, description, image, members.
 * ADMIN ONLY: remove members (trash icon) and delete group. Both need a connection.
 */
@Composable
fun EditGroupScreen(
    vm: AppViewModel,
    groupId: String,
    onBack: () -> Unit,
    onInvite: () -> Unit,
    onGroupGone: () -> Unit,
) {
    val group = vm.group(groupId)
    if (group == null) {
        MissingScreen(onBack)
        return
    }
    var name by rememberSaveable { mutableStateOf(group.name) }
    var description by rememberSaveable { mutableStateOf(group.description) }
    var confirmDelete by remember { mutableStateOf(false) }
    val iAmAdmin = group.adminId == ME
    val myNet = vm.myNet(groupId)
    val hasBalance = abs(myNet) >= 0.005

    AppScreen(
        title = "Edit group",
        onBack = onBack,
        actionText = "Cancel",
        onAction = onBack,
        offline = vm.offlineState(
            "Offline · Changes pending",
            "Kept on this device. Will retry when online. Connect to remove members or delete the group.",
        ),
    ) {
        AppTextField(name, { name = it }, "Group name")
        AppTextField(description, { description = it }, "Description")
        ClickableRow("Group image", trailing = "Change")

        SectionTitle("Members")
        group.memberIds.forEach { id ->
            val user = vm.user(id)
            ClickableRow(
                title = if (id == ME) "${user.name} (you)" else user.name,
                subtitle = user.email,
                trailingContent = {
                    when {
                        id == group.adminId -> Text("Admin")
                        iAmAdmin -> Row (
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Member")
                            IconButton(onClick = { vm.removeMember(groupId, id) }, enabled = vm.isOnline) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove ${user.name}")
                            }
                        }
                        else -> Text("Member")
                    }
                },
            )
        }
        SecondaryButton("+ Invite people", onClick = onInvite)
        PrimaryButton("Save changes", enabled = name.isNotBlank()) {
            vm.updateGroup(groupId, name, description)
            onBack()
        }

        if (iAmAdmin) {
            SecondaryButton("Delete group", enabled = vm.isOnline) { confirmDelete = true }
        } else {
            SecondaryButton("Leave group", enabled = !hasBalance) {
                vm.leaveGroup(groupId)
                onGroupGone()
            }
            if (hasBalance) {
                Text(
                    "Leave group is unavailable while you have an outstanding balance (${balanceText(myNet)}).",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete ${group.name}?") },
            text = { Text("This removes the group and all its expenses for everyone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.deleteGroup(groupId)
                    onGroupGone()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun EditGroupScreenPreview() {
    EditGroupScreen(
        vm = AppViewModel(),
        groupId = "g1",
        onBack = {},
        onInvite = {},
        onGroupGone = {},
    )
}