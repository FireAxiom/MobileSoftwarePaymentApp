package com.feelgoodinc.paymentapp.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.feelgoodinc.paymentapp.data.AppViewModel
import com.feelgoodinc.paymentapp.ui.auth.LoginScreen
import com.feelgoodinc.paymentapp.ui.auth.SignUpScreen
import com.feelgoodinc.paymentapp.ui.auth.StartScreen

/** Access screens 01–03. No bottom tabs here. */
fun NavGraphBuilder.authGraph(nav: NavController, vm: AppViewModel) {
    navigation(startDestination = Routes.START, route = Routes.AUTH) {

        composable(Routes.START) {
            StartScreen(
                onLogIn = { nav.navigate(Routes.LOGIN) },
                onSignUp = { nav.navigate(Routes.SIGN_UP) },
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                vm = vm,
                onBack = { nav.popBackStack() },
                onLoggedIn = { nav.enterApp() },
                // 02 ↔ 03 switch: replace one with the other so Back still goes to 01.
                onSwitchToSignUp = { nav.navigate(Routes.SIGN_UP) { popUpTo(Routes.LOGIN) { inclusive = true } } },
            )
        }

        composable(Routes.SIGN_UP) {
            SignUpScreen(
                vm = vm,
                onBack = { nav.popBackStack() },
                onSignedUp = { nav.enterApp() },
                onSwitchToLogIn = { nav.navigate(Routes.LOGIN) { popUpTo(Routes.SIGN_UP) { inclusive = true } } },
            )
        }
    }
}
