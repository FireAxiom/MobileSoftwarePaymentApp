package com.example.myapplication.ui.groups

import android.annotation.SuppressLint
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.data.AppViewModel
import com.example.myapplication.data.ME
import com.example.myapplication.data.balanceText
import com.example.myapplication.data.money
import com.example.myapplication.ui.components.AppScreen
import com.example.myapplication.ui.components.AppTextField
import com.example.myapplication.ui.components.GroupCard
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.offlineState
import com.example.myapplication.ui.theme.balanceColor

/** 05 Groups tab – search, select, create. */
@Composable
fun GroupsScreen(
    vm: AppViewModel,
    onOpenGroup: (groupId: String) -> Unit,
    onCreateGroup: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val shown = vm.groups.filter { it.name.contains(query.trim(), ignoreCase = true) }

    AppScreen(
        title = "Groups",
        offline = vm.offlineState(message = "Showing previously synced groups."),
        bottomContent = { PrimaryButton("+ Create group", onClick = onCreateGroup) },
    ) {
        AppTextField(query, { query = it }, "Search groups")
        if (shown.isEmpty()) {
            Text(if (vm.groups.isEmpty()) "No groups yet. Create one to get started." else "No groups match \"$query\".")
        }
        shown.forEach { group ->
            val members = group.memberIds.size
            val count = vm.expensesIn(group.id).size
            GroupCard(
                name = group.name,
                details = "$members member${if (members == 1) "" else "s"} · " +
                        "$count expense${if (count == 1) "" else "s"} · Total ${money(vm.groupTotal(group.id))}",
                balance = balanceText(vm.myNet(group.id)),
                balanceColor = balanceColor(vm.myNet(group.id)),
                balanceDetail = balanceDetail(vm, group.id),
            ) { onOpenGroup(group.id) }
        }
    }
}

/** "To Maya", or "Maya $32.00 · Leo $32.00" – who's behind the balance on the card. */
private fun balanceDetail(vm: AppViewModel, groupId: String): String? {
    val debts = vm.debts(groupId)
    val iOwe = debts.filter { it.fromId == ME }
    val owedToMe = debts.filter { it.toId == ME }
    return when {
        iOwe.size == 1 -> "To ${vm.firstName(iOwe.first().toId)}"
        iOwe.isNotEmpty() -> "To " + iOwe.joinToString(" · ") { "${vm.firstName(it.toId)} ${money(it.amount)}" }
        owedToMe.isNotEmpty() -> owedToMe.joinToString(" · ") { "${vm.firstName(it.fromId)} ${money(it.amount)}" }
        else -> null
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun GroupsScreenPreview() {
    GroupsScreen(
        vm = AppViewModel(),
        onOpenGroup = {},
        onCreateGroup = {},
    )
}