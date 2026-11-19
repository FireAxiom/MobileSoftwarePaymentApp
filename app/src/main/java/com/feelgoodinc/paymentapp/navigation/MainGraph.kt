package com.feelgoodinc.paymentapp.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.feelgoodinc.paymentapp.data.AppViewModel
import com.feelgoodinc.paymentapp.ui.balances.BalancesScreen
import com.feelgoodinc.paymentapp.ui.balances.PaymentRecordedScreen
import com.feelgoodinc.paymentapp.ui.balances.PaymentScreen
import com.feelgoodinc.paymentapp.ui.expenses.ExpenseDetailsScreen
import com.feelgoodinc.paymentapp.ui.expenses.ExpenseFormScreen
import com.feelgoodinc.paymentapp.ui.groups.CreateGroupScreen
import com.feelgoodinc.paymentapp.ui.groups.EditGroupScreen
import com.feelgoodinc.paymentapp.ui.groups.GroupScreen
import com.feelgoodinc.paymentapp.ui.groups.GroupsScreen
import com.feelgoodinc.paymentapp.ui.groups.InvitePeopleScreen
import com.feelgoodinc.paymentapp.ui.notifications.NotificationsScreen
import com.feelgoodinc.paymentapp.ui.profile.AccountSettingsScreen
import com.feelgoodinc.paymentapp.ui.profile.AppSettingsScreen
import com.feelgoodinc.paymentapp.ui.profile.NotificationSettingsScreen
import com.feelgoodinc.paymentapp.ui.profile.ProfileScreen

/**
 * The main app (after logging in). Starts on 05 Groups.
 * Each `composable(route) { ... }` = "when this route is opened, show this screen".
 * Screens never navigate themselves – they call callbacks, and THIS file decides where they go.
 */
fun NavGraphBuilder.mainGraph(nav: NavController, vm: AppViewModel) {
    val back: () -> Unit = { nav.popBackStack() }

    navigation(startDestination = Routes.GROUPS, route = Routes.MAIN) {

        // ---------- 04 Notifications tab (10 is a pop-up inside it) ----------
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                vm = vm,
                onOpenGroup = { nav.navigate(Routes.group(it)) },
                onOpenExpense = { nav.navigate(Routes.expense(it)) },
            )
        }

        // ---------- 05 Groups tab ----------
        composable(Routes.GROUPS) {
            GroupsScreen(
                vm = vm,
                onOpenGroup = { nav.navigate(Routes.group(it)) },
                onCreateGroup = { nav.navigate(Routes.CREATE_GROUP) },
            )
        }

        composable(Routes.GROUP) { entry ->
            val groupId = entry.arg("groupId")
            GroupScreen(
                vm = vm,
                groupId = groupId,
                onBack = back,
                onEdit = { nav.navigate(Routes.editGroup(groupId)) },
                onAddExpense = { nav.navigate(Routes.addExpense(groupId)) },
                onOpenExpense = { nav.navigate(Routes.expense(it)) },
                onOpenBalances = { nav.navigate(Routes.balances(groupId)) },
            )
        }

        composable(Routes.CREATE_GROUP) {
            CreateGroupScreen(
                vm = vm,
                onCancel = back,
                // Continue → 12. Remove 08 from history so Back doesn't create the group twice.
                onCreated = { groupId ->
                    nav.navigate(Routes.invitePeople(groupId)) { popUpTo(Routes.CREATE_GROUP) { inclusive = true } }
                },
            )
        }

        composable(Routes.EDIT_GROUP) { entry ->
            val groupId = entry.arg("groupId")
            EditGroupScreen(
                vm = vm,
                groupId = groupId,
                onBack = back,
                onInvite = { nav.navigate(Routes.invitePeople(groupId)) },
                onGroupGone = { nav.popBackStack(Routes.GROUPS, inclusive = false) }, // delete/leave → 05
            )
        }

        composable(Routes.INVITE_PEOPLE) { entry ->
            val groupId = entry.arg("groupId")
            InvitePeopleScreen(vm, groupId, onBack = back, onDone = { nav.backToGroup(groupId) })
        }

        // ---------- Expenses (13–19) ----------
        composable(Routes.ADD_EXPENSE) { entry ->
            ExpenseFormScreen(
                vm = vm,
                groupId = entry.arg("groupId"),
                expenseId = null,
                onCancel = back,
                // Save → 18. Replace 13 so Back from 18 goes to 07.
                onSaved = { expenseId ->
                    nav.navigate(Routes.expense(expenseId)) { popUpTo(Routes.ADD_EXPENSE) { inclusive = true } }
                },
            )
        }

        composable(Routes.EXPENSE) { entry ->
            val expenseId = entry.arg("expenseId")
            ExpenseDetailsScreen(vm, expenseId, onBack = back, onEdit = { nav.navigate(Routes.editExpense(expenseId)) })
        }

        composable(Routes.EDIT_EXPENSE) { entry ->
            ExpenseFormScreen(
                vm = vm,
                groupId = null,
                expenseId = entry.arg("expenseId"),
                onCancel = back,
                onSaved = { back() }, // Save → 18
            )
        }

        // ---------- Balances & settlement (20–23) ----------
        composable(Routes.BALANCES) { entry ->
            val groupId = entry.arg("groupId")
            BalancesScreen(
                vm = vm,
                groupId = groupId,
                onBack = back,
                onPay = { toUserId, partial -> nav.navigate(Routes.payment(groupId, toUserId, partial)) },
            )
        }

        composable(Routes.PAYMENT) { entry ->
            val groupId = entry.arg("groupId")
            val partial = entry.arg("mode") == "partial"
            PaymentScreen(
                vm = vm,
                groupId = groupId,
                toUserId = entry.arg("toUserId"),
                partial = partial,
                onCancel = back,
                onRecorded = { paymentId ->
                    if (partial) {
                        // 22 → 23 Payment recorded
                        nav.navigate(Routes.paymentRecorded(groupId, paymentId)) { popUpTo(Routes.PAYMENT) { inclusive = true } }
                    } else {
                        // 21 → straight back to 07 (settled)
                        nav.backToGroup(groupId)
                    }
                },
            )
        }

        composable(Routes.PAYMENT_RECORDED) { entry ->
            val groupId = entry.arg("groupId")
            PaymentRecordedScreen(vm, entry.arg("paymentId"), onDone = { nav.backToGroup(groupId) })
        }

        // ---------- 25 Profile tab ----------
        composable(Routes.PROFILE) {
            ProfileScreen(
                vm = vm,
                onAppSettings = { nav.navigate(Routes.APP_SETTINGS) },
                onAccountSettings = { nav.navigate(Routes.ACCOUNT_SETTINGS) },
                onLogOut = { nav.logOut() },
            )
        }

        composable(Routes.APP_SETTINGS) {
            AppSettingsScreen(vm, onBack = back, onNotificationSettings = { nav.navigate(Routes.NOTIFICATION_SETTINGS) })
        }

        composable(Routes.NOTIFICATION_SETTINGS) {
            NotificationSettingsScreen(vm, onBack = back)
        }

        composable(Routes.ACCOUNT_SETTINGS) {
            AccountSettingsScreen(vm, onBack = back, onAccountDeleted = { nav.logOut() })
        }
    }
}
