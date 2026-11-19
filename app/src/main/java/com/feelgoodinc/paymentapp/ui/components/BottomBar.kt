package com.feelgoodinc.paymentapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.feelgoodinc.paymentapp.navigation.Routes

/** The three shared bottom tabs: 04 Notifications, 05 Groups, 25 Profile. */
enum class Tab(val route: String, val label: String, val icon: ImageVector) {
    NOTIFICATIONS(Routes.NOTIFICATIONS, "Notifications", Icons.Default.Notifications),
    GROUPS(Routes.GROUPS, "Groups", Icons.Default.Home),
    PROFILE(Routes.PROFILE, "Profile", Icons.Default.Person),
}

@Composable
fun BottomBar(selected: Tab, onTabClick: (Tab) -> Unit) {
    NavigationBar {
        Tab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { onTabClick(tab) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(tab.label) },
            )
        }
    }
}
