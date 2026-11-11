package com.example.myapplication.ui.groups

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.AppViewModel
import com.example.myapplication.data.ME
import com.example.myapplication.data.balanceText
import com.example.myapplication.data.money
import com.example.myapplication.ui.components.AppScreen
import com.example.myapplication.ui.components.ClickableRow
import com.example.myapplication.ui.components.MissingScreen
import com.example.myapplication.ui.components.Placeholder
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SecondaryButton
import com.example.myapplication.ui.components.SectionTitle
import com.example.myapplication.ui.components.offlineState

/** 07 Individual group – the shared group destination. */
@Composable
fun GroupScreen(
    vm: AppViewModel,
    groupId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAddExpense: () -> Unit,
    onOpenExpense: (expenseId: String) -> Unit,
    onOpenBalances: () -> Unit,
) {
    val group = vm.group(groupId)
    if (group == null) {
        MissingScreen(onBack)
        return
    }
    val debts = vm.debts(groupId)

    AppScreen(
        title = group.name,
        onBack = onBack,
        actionText = "Edit",
        onAction = onEdit,
        offline = vm.offlineState(message = "Showing previously synced group data."),
    ) {
        Placeholder("Group cover", 100.dp)
        Text("${group.memberIds.size} members · ${group.memberIds.joinToString { vm.firstName(it) }}")
        if (group.description.isNotBlank()) Text(group.description, style = MaterialTheme.typography.bodySmall)

        OutlinedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(balanceText(vm.myNet(groupId)), style = MaterialTheme.typography.titleMedium)
                debts.filter { it.fromId == ME }.forEach { Text("You owe ${money(it.amount)} to ${vm.user(it.toId).name}") }
                debts.filter { it.toId == ME }.forEach { Text("${vm.user(it.fromId).name} owes you ${money(it.amount)}") }
                SecondaryButton("View balances / settle up", onClick = onOpenBalances)
            }
        }

        SectionTitle("Activity")
        val expenses = vm.expensesIn(groupId)
        if (expenses.isEmpty()) Text("No expenses yet.")
        expenses.forEach { e ->
            ClickableRow(
                title = e.description,
                subtitle = "${vm.firstName(e.paidById)} paid ${money(e.amount)} · ${e.date}",
                trailing = "${money(e.amount / e.splitBetween.size)} each",
            ) { onOpenExpense(e.id) }
        }
        vm.paymentsIn(groupId).forEach { p ->
            ClickableRow(
                title = "Payment",
                subtitle = "${vm.firstName(p.fromId)} paid ${vm.firstName(p.toId)} · ${p.date}",
                trailing = money(p.amount),
            )
        }
        PrimaryButton("+ Add expense", onClick = onAddExpense)
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun GroupScreenPreview() {
    GroupScreen(
        vm = AppViewModel(),
        groupId = "g1",
        onBack = {},
        onEdit = {},
        onAddExpense = {},
        onOpenExpense = {},
        onOpenBalances = {},
    )
}