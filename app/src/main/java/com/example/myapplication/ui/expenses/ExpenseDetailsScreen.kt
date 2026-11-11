package com.example.myapplication.ui.expenses

import android.annotation.SuppressLint
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.AppViewModel
import com.example.myapplication.data.money
import com.example.myapplication.ui.components.AppScreen
import com.example.myapplication.ui.components.LabelValue
import com.example.myapplication.ui.components.MissingScreen
import com.example.myapplication.ui.components.Placeholder
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.SecondaryButton
import com.example.myapplication.ui.components.SectionTitle
import com.example.myapplication.ui.components.offlineState

/** 18 Expense details – view, Edit → 19, Back → wherever it was opened from (07 or 04). */
@Composable
fun ExpenseDetailsScreen(
    vm: AppViewModel,
    expenseId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    val expense = vm.expense(expenseId)
    if (expense == null) {
        MissingScreen(onBack)
        return
    }
    var showReceipt by remember { mutableStateOf(false) }
    val share = expense.amount / expense.splitBetween.size

    AppScreen(
        title = expense.description,
        onBack = onBack,
        actionText = "Edit",
        onAction = onEdit,
        offline = vm.offlineState(message = "Showing previously synced expense details."),
    ) {
        Text(money(expense.amount), style = MaterialTheme.typography.displaySmall)
        Text("${vm.group(expense.groupId)?.name.orEmpty()} · ${expense.date}")
        Text("Paid by ${vm.displayName(expense.paidById)}")

        SectionTitle("Split equally")
        expense.splitBetween.forEach { LabelValue(vm.displayName(it), money(share)) }

        // 18 · View receipt – an action, not a separate screen. Unavailable offline.
        SectionTitle("Receipt")
        when {
            !expense.hasReceipt -> Text("No receipt attached.")
            !vm.isOnline -> Text("Receipt unavailable offline. Connect to the internet to view this receipt.")
            else -> SecondaryButton("View receipt") { showReceipt = true }
        }

        if (expense.note.isNotBlank()) {
            SectionTitle("Note")
            Text(expense.note)
        }
        PrimaryButton("Edit expense", onClick = onEdit)
    }

    if (showReceipt) {
        AlertDialog(
            onDismissRequest = { showReceipt = false },
            title = { Text("Receipt") },
            text = { Placeholder("receipt.jpg", 200.dp) },
            confirmButton = { TextButton(onClick = { showReceipt = false }) { Text("Close") } },
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun ExpenseDetailsScreenPreview() {
    ExpenseDetailsScreen(
        vm = AppViewModel(),
        expenseId = "e1",
        onBack = {},
        onEdit = {},
    )
}
