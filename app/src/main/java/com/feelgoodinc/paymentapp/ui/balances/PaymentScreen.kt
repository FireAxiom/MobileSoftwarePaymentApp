package com.feelgoodinc.paymentapp.ui.balances

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feelgoodinc.paymentapp.data.AppViewModel
import com.feelgoodinc.paymentapp.data.ME
import com.feelgoodinc.paymentapp.data.money
import com.feelgoodinc.paymentapp.data.today
import com.feelgoodinc.paymentapp.ui.components.AppScreen
import com.feelgoodinc.paymentapp.ui.components.AppTextField
import com.feelgoodinc.paymentapp.ui.components.LabelValue
import com.feelgoodinc.paymentapp.ui.components.MissingScreen
import com.feelgoodinc.paymentapp.ui.components.PrimaryButton
import com.feelgoodinc.paymentapp.ui.components.SecondaryButton

/**
 * 21 Full payment AND 22 Partial payment – same screen, `partial` decides which.
 * Only RECORDS a payment made outside the app.
 */
@Composable
fun PaymentScreen(
    vm: AppViewModel,
    groupId: String,
    toUserId: String,
    partial: Boolean,
    onCancel: () -> Unit,
    onRecorded: (paymentId: String) -> Unit,
) {
    val group = vm.group(groupId)
    if (group == null) {
        MissingScreen(onCancel)
        return
    }
    val outstanding = vm.debts(groupId).find { it.fromId == ME && it.toId == toUserId }?.amount ?: 0.0
    var amountText by rememberSaveable { mutableStateOf("") }
    val amount = if (partial) amountText.replace(',', '.').toDoubleOrNull() ?: 0.0 else outstanding
    val valid = amount > 0 && amount <= outstanding + 0.001

    AppScreen(title = if (partial) "Partial payment" else "Full payment", onBack = onCancel) {
        OutlinedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(group.name, style = MaterialTheme.typography.titleMedium)
                LabelValue("From", "${vm.currentUser.name} (you)")
                LabelValue("To", vm.user(toUserId).name)
                LabelValue("Outstanding", money(outstanding))
                if (!partial) LabelValue("This payment", money(outstanding))
            }
        }
        LabelValue("Payment date", today())

        if (partial) {
            AppTextField(amountText, { amountText = it }, "Amount to record · USD", keyboardType = KeyboardType.Decimal)
            Text("Enter more than $0.00 and no more than ${money(outstanding)}.", style = MaterialTheme.typography.bodySmall)
            LabelValue("Remaining after payment", money((outstanding - amount).coerceAtLeast(0.0)))
        } else {
            LabelValue("Your remaining balance", money(0.0))
        }

        Text(
            "Record a payment you have already made outside the app. No money is transferred.",
            style = MaterialTheme.typography.bodySmall,
        )
        PrimaryButton(
            if (partial) "Record payment — ${money(amount)}" else "Record full payment — ${money(outstanding)}",
            enabled = valid,
        ) { onRecorded(vm.recordPayment(groupId, toUserId, amount)) }
        SecondaryButton("Cancel", onClick = onCancel)
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun PaymentScreenPreview() {
    PaymentScreen(
        vm = AppViewModel(),
        groupId = "g1",
        toUserId = "",
        partial = false,  // change to either true or false
        onCancel = {},
        onRecorded = {},
    )
}
