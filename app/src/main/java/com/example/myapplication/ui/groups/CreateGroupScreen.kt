package com.example.myapplication.ui.groups

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.AppViewModel
import com.example.myapplication.ui.components.AppScreen
import com.example.myapplication.ui.components.AppTextField
import com.example.myapplication.ui.components.Placeholder
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.offlineState

/** 08 Create group – create the group, then continue to 12 Invite people. */
@Composable
fun CreateGroupScreen(
    vm: AppViewModel,
    onCancel: () -> Unit,
    onCreated: (groupId: String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    AppScreen(
        title = "Create group",
        onBack = onCancel,
        actionText = "Cancel",
        onAction = onCancel,
        offline = vm.offlineState("Offline · Group creation pending", "Draft kept on this device. Will retry when online."),
    ) {
        Placeholder("+ Add group image (optional)", 100.dp)
        AppTextField(name, { name = it }, "Group name")
        AppTextField(description, { description = it }, "Description (optional)")
        PrimaryButton("Create group & invite people", enabled = name.isNotBlank()) {
            onCreated(vm.createGroup(name, description))
        }
    }
}
