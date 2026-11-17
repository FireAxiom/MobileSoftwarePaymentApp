package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * The standard layout every screen uses:
 *   top bar  →  offline banner (only when offline)  →  scrollable content
 */
@Composable
fun AppScreen(
    title: String,
    onBack: (() -> Unit)? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    offline: OfflineState? = null,
    bottomContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        topBar = { TopBar(title, onBack, actionText, onAction) },
        bottomBar = {
            if (bottomContent != null) {
                Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) { bottomContent() }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (offline != null) OfflineBanner(offline)
            content()
        }
    }
}

/** Shown if a screen is opened for something that no longer exists (e.g. a deleted group). */
@Composable
fun MissingScreen(onBack: () -> Unit) {
    AppScreen(title = "Not found", onBack = onBack) {
        Text("This item is no longer available.")
    }
}
