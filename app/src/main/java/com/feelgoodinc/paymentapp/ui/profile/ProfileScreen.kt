package com.feelgoodinc.paymentapp.ui.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.feelgoodinc.paymentapp.data.AppViewModel
import com.feelgoodinc.paymentapp.ui.components.AppScreen
import com.feelgoodinc.paymentapp.ui.components.ClickableRow
import com.feelgoodinc.paymentapp.ui.components.SecondaryButton
import com.feelgoodinc.paymentapp.ui.components.SectionTitle

/** 25 Profile tab – settings, account, invites, log out. */
@Composable
fun ProfileScreen(
    vm: AppViewModel,
    onAppSettings: () -> Unit,
    onAccountSettings: () -> Unit,
    onLogOut: () -> Unit,
) {
    AppScreen(title = "Profile") {
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .size(96.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Person, contentDescription = "Profile photo", Modifier.size(48.dp))
        }
        Text(vm.currentUser.name, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(vm.currentUser.email, modifier = Modifier.align(Alignment.CenterHorizontally))

        SectionTitle("Your account")
        ClickableRow("App & notification settings", trailing = "›", onClick = onAppSettings)
        ClickableRow("Account settings", trailing = "›", onClick = onAccountSettings)
        // 25 · Manage invites: entry exists, but the model has no destination screen for it yet.
        ClickableRow("Manage invites", "${vm.sentInvites.size} pending · Not available yet")
        SecondaryButton("Log out", onClick = onLogOut)
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        vm = AppViewModel(),
        onAppSettings = {},
        onAccountSettings = {},
        onLogOut = {},
    )
}
