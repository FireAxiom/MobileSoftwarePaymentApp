package com.example.myapplication.ui.expenses

import android.annotation.SuppressLint
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.data.AppViewModel
import com.example.myapplication.data.ME
import com.example.myapplication.data.money
import com.example.myapplication.ui.components.AppScreen
import com.example.myapplication.ui.components.AppTextField
import com.example.myapplication.ui.components.ClickableRow
import com.example.myapplication.ui.components.MissingScreen
import com.example.myapplication.ui.components.PrimaryButton
import com.example.myapplication.ui.components.offlineState

/** Which pop-up (14–17) is open over the form. */
private enum class Overlay { AMOUNT, PAYER, SPLIT, RECEIPT }

/**
 * 13 Add expense AND 19 Edit expense – same form.
 *  - Add:  groupId given, expenseId = null
 *  - Edit: expenseId given
 * Tapping a field opens an overlay (14–17); Done/close returns here with the chosen value.
 */
@Composable
fun ExpenseFormScreen(
    vm: AppViewModel,
    groupId: String?,
    expenseId: String?,
    onCancel: () -> Unit,
    onSaved: (expenseId: String) -> Unit,
) {
    val existing = expenseId?.let { vm.expense(it) }
    val group = vm.group(existing?.groupId ?: groupId.orEmpty())
    if (group == null || (expenseId != null && existing == null)) {
        MissingScreen(onCancel)
        return
    }
    val isEdit = existing != null
    val members = vm.membersOf(group)

    var description by rememberSaveable { mutableStateOf(existing?.description ?: "") }
    var amount by rememberSaveable { mutableDoubleStateOf(existing?.amount ?: 0.0) }
    var paidBy by rememberSaveable { mutableStateOf(existing?.paidById ?: ME) }
    var splitBetween by remember { mutableStateOf(existing?.splitBetween ?: group.memberIds) }
    var note by rememberSaveable { mutableStateOf(existing?.note ?: "") }
    var hasReceipt by rememberSaveable { mutableStateOf(existing?.hasReceipt ?: false) }
    var overlay by remember { mutableStateOf<Overlay?>(null) }

    val canSave = description.isNotBlank() && amount > 0 && splitBetween.isNotEmpty()

    AppScreen(
        title = if (isEdit) "Edit expense" else "Add expense",
        onBack = onCancel,
        actionText = "Cancel",
        onAction = onCancel,
        offline = vm.offlineState(
            if (isEdit) "Offline · Changes pending" else "Offline · Expense pending",
            "Changes kept on this device. Will retry when online.",
        ),
    ) {
        Text("${group.name.uppercase()} · ${vm.settings.currency}", style = MaterialTheme.typography.labelMedium)
        AppTextField(description, { description = it }, "Description")
        // 13/19 · rows follow the wireframe: value as title, explanation as subtitle, action on the right.
        ClickableRow(money(amount), "Amount in ${vm.settings.currency}", trailing = "Edit ›") { overlay = Overlay.AMOUNT }
        HorizontalDivider()
        ClickableRow(
            title = "Paid by ${if (paidBy == ME) "you" else vm.firstName(paidBy)}",
            subtitle = "One person paid the full amount",
            trailing = "Change ›",
        ) { overlay = Overlay.PAYER }
        HorizontalDivider()
        ClickableRow(
            title = "Split equally · ${splitBetween.size} people",
            subtitle = splitSummary(splitBetween.map { vm.firstName(it) }) +
                if (splitBetween.isNotEmpty()) " · ${money(amount / splitBetween.size)} each" else "",
            trailing = "Change ›",
        ) { overlay = Overlay.SPLIT }
        HorizontalDivider()
        ClickableRow(
            title = "Receipt (optional)",
            subtitle = if (hasReceipt) "receipt.jpg · attached" else "No receipt attached",
            trailing = if (hasReceipt) "View ›" else "Add ›",
        ) { overlay = Overlay.RECEIPT }
        HorizontalDivider()
        AppTextField(note, { note = it }, "Note (optional)", singleLine = false)

        PrimaryButton(if (isEdit) "Save changes" else "Save expense", enabled = canSave) {
            if (existing != null) {
                vm.updateExpense(
                    existing.copy(
                        description = description.trim(),
                        amount = amount,
                        paidById = paidBy,
                        splitBetween = splitBetween,
                        note = note.trim(),
                        hasReceipt = hasReceipt,
                    ),
                )
                onSaved(existing.id)
            } else {
                onSaved(vm.addExpense(group.id, description.trim(), amount, paidBy, splitBetween, note.trim(), hasReceipt))
            }
        }
    }

    // Overlays 14–17. Not a sequence – each one just updates one field and closes.
    when (overlay) {
        Overlay.AMOUNT -> AmountOverlay(amount, onDone = { amount = it; overlay = null }, onDismiss = { overlay = null })
        Overlay.PAYER -> PayerOverlay(members, paidBy, amount, { vm.fullName(it) }, onDone = { paidBy = it; overlay = null }, onDismiss = { overlay = null })
        Overlay.SPLIT -> SplitOverlay(members, splitBetween, amount, { vm.fullName(it) }, onDone = { splitBetween = it; overlay = null }, onDismiss = { overlay = null })
        Overlay.RECEIPT -> ReceiptOverlay(hasReceipt, onDone = { hasReceipt = it; overlay = null }, onDismiss = { overlay = null })
        null -> Unit
    }
}

/** "You, Maya and Leo" – the wireframe's natural-language list. */
private fun splitSummary(names: List<String>): String = when (names.size) {
    0 -> "Nobody selected"
    1 -> names[0]
    else -> names.dropLast(1).joinToString() + " and " + names.last()
}

/** "Alex Morgan (you)" for the current user, the full name for everyone else (15, 16, 18). */
internal fun AppViewModel.fullName(id: String): String =
    if (id == ME) "${user(ME).name} (you)" else user(id).name

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun ExpenseFormScreenPreview() {
    ExpenseFormScreen(
        vm = AppViewModel(),
        groupId = "g1",
        expenseId = "e1",
        onCancel = {},
        onSaved = {},
    )
}
