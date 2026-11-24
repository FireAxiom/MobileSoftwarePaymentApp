package com.example.myapplication.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.AppViewModel
import com.example.myapplication.ui.components.BottomBar
import com.example.myapplication.ui.components.Tab

/**
 * The whole app shell:
 *   Scaffold  = page layout with the bottom tab bar
 *   NavHost   = swaps the screen above the bar
 */
@Composable
fun AppNavHost(vm: AppViewModel = viewModel()) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    // Highlight the tab the user last opened (stays highlighted while going deeper in that tab).
    var selectedTab by rememberSaveable { mutableStateOf(Tab.GROUPS) }
    LaunchedEffect(route) {
        Tab.entries.firstOrNull { it.route == route }?.let { selectedTab = it }
    }
    val showBottomBar = route != null && route !in Routes.screensWithoutTabs

    // Request feedback from AppViewModel: saved / failed, will retry / synced (NFR-05–07).
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(vm.feedback) {
        val message = vm.feedback ?: return@LaunchedEffect
        vm.feedbackShown()
        snackbarHostState.showSnackbar(message)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                BottomBar(selectedTab) { tab -> navController.switchTab(tab.route) }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.AUTH,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding),
        ) {
            authGraph(navController, vm)
            mainGraph(navController, vm)
        }
    }
}

// ---------- Small navigation helpers used by the graphs ----------

/** Standard bottom-tab behavior: don't stack tabs on top of each other, remember each tab's screens. */
fun NavController.switchTab(route: String) = navigate(route) {
    popUpTo(Routes.GROUPS) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

/** After log in / sign up: go to the main app and remove the access screens from history. */
fun NavController.enterApp() = navigate(Routes.MAIN) {
    popUpTo(Routes.AUTH) { inclusive = true }
}

/** Log out: back to 01 Start page and clear the main app from history. */
fun NavController.logOut() = navigate(Routes.AUTH) {
    popUpTo(Routes.MAIN) { inclusive = true }
}

/** "Return → 07": go back to the group if it's in history, otherwise open it on top of 05 Groups. */
fun NavController.backToGroup(groupId: String) {
    if (!popBackStack(Routes.GROUP, inclusive = false)) {
        navigate(Routes.group(groupId)) { popUpTo(Routes.GROUPS) }
    }
}

/** Read a {placeholder} value from the current route. */
fun NavBackStackEntry.arg(name: String): String = arguments?.getString(name).orEmpty()
