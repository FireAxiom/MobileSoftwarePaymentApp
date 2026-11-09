package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.AppViewModel

data class OfflineState(val title: String, val message: String, val lastSynced: String)

/**
 * Returns the banner content when offline, or null when online.
 * Usage on a screen: AppScreen(..., offline = vm.offlineState(message = "..."))
 */
fun AppViewModel.offlineState(
    title: String = "Offline",
    message: String,
    lastSynced: String? = null,
): OfflineState? = if (isOnline) null else OfflineState(title, message, lastSynced ?: this.lastSynced)

/** Amber banner shown first in the content, directly below the header (see navigation model). */
@Composable
fun OfflineBanner(state: OfflineState, modifier: Modifier = Modifier) {
    Surface(
        color = Color(0xFFFFF3E0),
        contentColor = Color(0xFF5D4037),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(state.title, style = MaterialTheme.typography.titleSmall)
            Text("Last synced: ${state.lastSynced}", style = MaterialTheme.typography.bodySmall)
            Text(state.message, style = MaterialTheme.typography.bodySmall)
        }
    }
}
