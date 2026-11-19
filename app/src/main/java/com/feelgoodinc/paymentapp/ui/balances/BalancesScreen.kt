package com.feelgoodinc.paymentapp.ui.balances

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feelgoodinc.paymentapp.data.AppViewModel
import com.feelgoodinc.paymentapp.data.ME
import com.feelgoodinc.paymentapp.data.money
import com.feelgoodinc.paymentapp.data.netText
import com.feelgoodinc.paymentapp.ui.components.AppScreen
import com.feelgoodinc.paymentapp.ui.components.ClickableRow
import com.feelgoodinc.paymentapp.ui.components.MissingScreen
import com.feelgoodinc.paymentapp.ui.components.PrimaryButton
import com.feelgoodinc.paymentapp.ui.components.SecondaryButton
import com.feelgoodinc.paymentapp.ui.components.SectionTitle
import com.feelgoodinc.paymentapp.ui.components.offlineState

/** 20 Owing and owed – member balances, Pay full (21) / partial (22), remind member (bell). */
@Composable
fun BalancesScreen(
    vm: AppViewModel,
    groupId: String,
    onBack: () -> Unit,
    onPay: (toUserId: String, partial: Boolean) -> Unit,
) {
    val group = vm.group(groupId)
    if (group == null) {
        MissingScreen(onBack)
        return
    }
    val debts = vm.debts(groupId)
    val myDebts = debts.filter { it.fromId == ME }
    val owedToMe = debts.filter { it.toId == ME }
    var reminded by remember { mutableStateOf(setOf<String>()) }

    AppScreen(
        title = "Group balances",
        onBack = onBack,
        offline = vm.offlineState(message = "Showing previously synced balances."),
    ) {
        Text("${group.name} · ${vm.settings.currency}", style = MaterialTheme.typography.labelLarge)

        if (myDebts.isEmpty()) {
            Text(if (owedToMe.isEmpty()) "You're settled up in this group." else "You don't owe anyone in this group.")
        }
        myDebts.forEach { debt ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("You owe ${vm.user(debt.toId).name} ${money(debt.amount)}", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Your share ${money(vm.shareOf(groupId, ME))} · You paid ${money(vm.paidBy(groupId, ME))}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    PrimaryButton("Pay full amount — ${money(debt.amount)}") { onPay(debt.toId, false) }
                    SecondaryButton("Pay a partial amount") { onPay(debt.toId, true) }
                }
            }
        }
        Text(
            "Record payments already made outside the app. No money is transferred in the app.",
            style = MaterialTheme.typography.bodySmall,
        )

        SectionTitle("Everyone's net balance")
        group.memberIds.forEach { id ->
            val owesMe = owedToMe.any { it.fromId == id }
            ClickableRow(
                title = vm.displayName(id),
                subtitle = "Paid ${money(vm.paidBy(groupId, id))} · Share ${money(vm.shareOf(groupId, id))}",
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 20 · Remind member: bell only next to people who owe me.
                        if (owesMe) {
                            IconButton(onClick = { reminded = reminded + id }) {
                                Icon(
                                    if (id in reminded) Icons.Default.Check else Icons.Default.Notifications,
                                    contentDescription = "Remind ${vm.user(id).name}",
                                )
                            }
                        }
                        Text(netText(vm.netBalance(groupId, id)))
                    }
                },
            )
        }
        if (reminded.isNotEmpty()) {
            Text("Reminder sent to ${reminded.joinToString { vm.firstName(it) }}.", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun BalancesScreenPreview() {
    BalancesScreen(
        vm = AppViewModel(),
        groupId = "g1",
        onBack = {},
        onPay = { _, _ -> },
    )
}
