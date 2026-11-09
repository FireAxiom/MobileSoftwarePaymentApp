package com.example.myapplication.ui.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.User
import com.example.myapplication.data.money
import com.example.myapplication.data.round2
import com.example.myapplication.ui.components.AppTextField
import com.example.myapplication.ui.components.LabelValue
import com.example.myapplication.ui.components.OverlaySheet
import com.example.myapplication.ui.components.Placeholder
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SecondaryButton
import java.util.Locale

/** 14 Expense amount overlay. */
@Composable
fun AmountOverlay(initial: Double, onDone: (Double) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(if (initial > 0) "%.2f".format(Locale.US, initial) else "") }
    val value = text.replace(',', '.').toDoubleOrNull() ?: 0.0

    OverlaySheet("Enter amount", onDismiss) {
        AppTextField(text, { text = it }, "Amount · USD", keyboardType = KeyboardType.Decimal)
        Text("Total paid for this expense", style = MaterialTheme.typography.bodySmall)
        PrimaryButton("Done — use ${money(value)}", enabled = value > 0) { onDone(round2(value)) }
    }
}

/** 15 Choose who paid overlay. */
@Composable
fun PayerOverlay(
    members: List<User>,
    selected: String,
    nameOf: (String) -> String,
    onDone: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var choice by remember { mutableStateOf(selected) }

    OverlaySheet("Who paid?", onDismiss) {
        Text("Select the person who paid the bill.")
        members.forEach { user ->
            Row(
                Modifier.fillMaxWidth().clickable { choice = user.id },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = choice == user.id, onClick = { choice = user.id })
                Text(nameOf(user.id))
            }
        }
        PrimaryButton("Done — paid by ${nameOf(choice)}") { onDone(choice) }
    }
}

/** 16 Choose split overlay (equal split between the ticked people). */
@Composable
fun SplitOverlay(
    members: List<User>,
    selected: List<String>,
    amount: Double,
    nameOf: (String) -> String,
    onDone: (List<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    var chosen by remember { mutableStateOf(selected.toSet()) }
    val each = if (chosen.isEmpty()) 0.0 else amount / chosen.size
    fun toggle(id: String) {
        chosen = if (id in chosen) chosen - id else chosen + id
    }

    OverlaySheet("Split between", onDismiss) {
        LabelValue("Split method", "Equally")
        members.forEach { user ->
            Row(
                Modifier.fillMaxWidth().clickable { toggle(user.id) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = user.id in chosen, onCheckedChange = { toggle(user.id) })
                Text(nameOf(user.id), Modifier.weight(1f))
                Text(if (user.id in chosen) money(each) else "—")
            }
        }
        LabelValue("Total allocated", "${money(each * chosen.size)} / ${money(amount)}")
        PrimaryButton("Done — ${chosen.size} people, ${money(each)} each", enabled = chosen.isNotEmpty()) {
            onDone(members.map { it.id }.filter { it in chosen }) // keep member order
        }
    }
}

/** 17 Optional receipt overlay. Camera/library are faked – they just mark a receipt as attached. */
@Composable
fun ReceiptOverlay(hasReceipt: Boolean, onDone: (Boolean) -> Unit, onDismiss: () -> Unit) {
    var attached by remember { mutableStateOf(hasReceipt) }

    OverlaySheet("Add a receipt (optional)", onDismiss) {
        Placeholder(if (attached) "receipt.jpg · Ready to attach" else "Receipt preview", 160.dp)
        SecondaryButton("Take photo") { attached = true }
        SecondaryButton("Choose from library") { attached = true }
        PrimaryButton("Attach receipt", enabled = attached) { onDone(true) }
        TextButton(onClick = { onDone(false) }, modifier = Modifier.fillMaxWidth()) {
            Text("Skip — continue without receipt")
        }
    }
}
