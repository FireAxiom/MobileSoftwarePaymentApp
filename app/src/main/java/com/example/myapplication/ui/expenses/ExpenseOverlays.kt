package com.example.myapplication.ui.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.DebtCalculation
import com.example.myapplication.data.ME
import com.example.myapplication.data.User
import com.example.myapplication.data.money
import com.example.myapplication.data.parseCents
import com.example.myapplication.data.perPersonText
import com.example.myapplication.data.plainAmount
import com.example.myapplication.ui.components.ClickableRow
import com.example.myapplication.ui.components.LabelValue
import com.example.myapplication.ui.components.OverlaySheet
import com.example.myapplication.ui.components.Placeholder
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SecondaryButton

private const val BACKSPACE = "⌫"
private val KEYPAD = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf(".", "0", BACKSPACE),
)

/** Applies one keypad press to the typed amount: max 2 decimals, one dot, no leading zeros. */
private fun pressKey(text: String, key: String): String = when {
    key == BACKSPACE -> text.dropLast(1)
    key == "." -> when {
        "." in text -> text
        text.isEmpty() -> "0."
        else -> "$text."
    }
    "." in text && text.substringAfter(".").length >= 2 -> text
    "." !in text && text.length >= 7 -> text // max $9,999,999
    text == "0" -> key
    else -> text + key
}

/** 14 Expense amount overlay – amount display + on-screen keypad, as in the wireframe. */
@Composable
fun AmountOverlay(initial: Long, onDone: (Long) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(if (initial > 0) plainAmount(initial) else "") }
    val value = parseCents(text) ?: 0L

    OverlaySheet("Enter amount", onDismiss) {
        Text(
            "Amount · USD",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedCard(Modifier.fillMaxWidth()) {
            Text(
                "$" + text.ifEmpty { "0.00" },
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp),
            )
        }
        Text("Total paid for this expense", style = MaterialTheme.typography.bodySmall)
        KEYPAD.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    SecondaryButton(key, modifier = Modifier.weight(1f)) { text = pressKey(text, key) }
                }
            }
        }
        PrimaryButton("Done — use ${money(value)}", enabled = value > 0) { onDone(value) }
    }
}

/** 15 Choose who paid overlay. */
@Composable
fun PayerOverlay(
    members: List<User>,
    selected: String,
    amount: Long,
    nameOf: (String) -> String,
    onDone: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var choice by remember { mutableStateOf(selected) }

    OverlaySheet("Who paid?", onDismiss) {
        Text("Select the person who paid the bill.")
        members.forEach { user ->
            ClickableRow(
                title = nameOf(user.id),
                subtitle = if (choice == user.id) "Paying ${money(amount)}" else null,
                trailingContent = { RadioButton(selected = choice == user.id, onClick = { choice = user.id }) },
            ) { choice = user.id }
            HorizontalDivider()
        }
        val payer = if (choice == ME) "you" else nameOf(choice).substringBefore(" ")
        PrimaryButton("Done — paid by $payer") { onDone(choice) }
    }
}

/** 16 Choose split overlay (equal split between the ticked people). */
@Composable
fun SplitOverlay(
    members: List<User>,
    selected: List<String>,
    amount: Long,
    nameOf: (String) -> String,
    onDone: (List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    var chosen by remember { mutableStateOf(selected.toSet()) }
    val shares = DebtCalculation.split(amount, chosen.toList()) // exact cents, adds up to amount
    fun toggle(id: String) {
        chosen = if (id in chosen) chosen - id else chosen + id
    }

    OverlaySheet("Split between", onDismiss) {
        LabelValue("Split method", "Equally")
        HorizontalDivider()
        members.forEach { user ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = user.id in chosen, onCheckedChange = { toggle(user.id) })
                ClickableRow(
                    title = nameOf(user.id),
                    trailing = shares[user.id]?.let(::money) ?: "—",
                ) { toggle(user.id) }
            }
            HorizontalDivider()
        }
        LabelValue("Total allocated", "${money(shares.values.sum())} / ${money(amount)}")
        HorizontalDivider()
        PrimaryButton("Done — ${chosen.size} people, ${perPersonText(amount, chosen.size)}", enabled = chosen.isNotEmpty()) {
            onDone(members.map { it.id }.filter { it in chosen }) // keep member order
        }
    }
}

/** 17 Optional receipt overlay. Camera/library are faked – they just mark a receipt as attached. */
@Composable
fun ReceiptOverlay(hasReceipt: Boolean, onDone: (Boolean) -> Unit, onDismiss: () -> Unit) {
    var attached by remember { mutableStateOf(hasReceipt) }

    OverlaySheet("Add a receipt (optional)", onDismiss) {
        Placeholder("Receipt preview", 160.dp)
        if (attached) Text("receipt.jpg · Ready to attach", style = MaterialTheme.typography.bodySmall)
        SecondaryButton("Take photo") { attached = true }
        SecondaryButton("Choose from library") { attached = true }
        PrimaryButton("Attach receipt", enabled = attached) { onDone(true) }
        SecondaryButton("Skip — continue without receipt") { onDone(false) }
    }
}
