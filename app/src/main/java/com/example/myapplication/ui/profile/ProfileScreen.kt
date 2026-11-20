package com.example.myapplication.ui.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.AppViewModel
import com.example.myapplication.ui.components.AppScreen
import com.example.myapplication.ui.components.ClickableRow
import com.example.myapplication.ui.components.Placeholder
import com.example.myapplication.ui.components.SecondaryButton
import com.example.myapplication.ui.components.SectionTitle

/** 25 Profile tab – settings, account, invites, log out. */
@Composable
fun ProfileScreen(
    vm: AppViewModel,
    onAppSettings: () -> Unit,
    onAccountSettings: () -> Unit,
    onLogOut: () -> Unit,
) {
    AppScreen(title = "Profile") {
        Placeholder("Profile photo", 100.dp)
        Text(vm.currentUser.name, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.align(Alignment.Start))
        Text(vm.currentUser.email, modifier = Modifier.align(Alignment.Start))

        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
        )
        {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp))
            {
                SectionTitle("Your account")
                ClickableRow("App & notification settings", trailing = "›", onClick = onAppSettings)
                ClickableRow("Account settings", trailing = "›", onClick = onAccountSettings)
                // 25 · Manage invites: entry exists, but the model has no destination screen for it yet.
                ClickableRow("Manage invites", "${vm.sentInvites.size} pending")
            }
        }
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
