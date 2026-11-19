package com.feelgoodinc.paymentapp.ui.balances

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feelgoodinc.paymentapp.data.AppViewModel
import com.feelgoodinc.paymentapp.data.ME
import com.feelgoodinc.paymentapp.data.money
import com.feelgoodinc.paymentapp.ui.components.AppScreen
import com.feelgoodinc.paymentapp.ui.components.LabelValue
import com.feelgoodinc.paymentapp.ui.components.MissingScreen
import com.feelgoodinc.paymentapp.ui.components.PrimaryButton
import com.feelgoodinc.paymentapp.ui.components.SectionTitle

/** 23 Payment recorded (after a partial payment). Done → 07 Individual group. */
@Composable
fun PaymentRecordedScreen(vm: AppViewModel, paymentId: String, onDone: () -> Unit) {
    val payment = vm.payment(paymentId)
    if (payment == null) {
        MissingScreen(onDone)
        return
    }
    val stillToPay = vm.debts(payment.groupId).find { it.fromId == ME && it.toId == payment.toId }?.amount ?: 0.0

    AppScreen(title = "Payment recorded", onBack = onDone) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary)
        Text("${money(payment.amount)} paid to ${vm.firstName(payment.toId)}", style = MaterialTheme.typography.headlineSmall)
        Text("${vm.group(payment.groupId)?.name.orEmpty()} · ${payment.date}")

        SectionTitle("Your balance is updated")
        LabelValue("Previously owed", money(stillToPay + payment.amount))
        LabelValue("Payment recorded", "−${money(payment.amount)}")
        LabelValue("Still to pay", money(stillToPay))
        PrimaryButton("Done — back to group", onClick = onDone)
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun PaymentRecordedScreenPreview() {
    PaymentRecordedScreen(
        vm = AppViewModel(),
        paymentId = "p1",
        onDone = {},
    )
}
