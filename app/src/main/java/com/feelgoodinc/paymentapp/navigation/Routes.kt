package com.feelgoodinc.paymentapp.navigation

/**
 * Every screen's "address". Numbers match the UX navigation model.
 * Routes with {braces} take arguments – use the helper functions to fill them in.
 */
object Routes {
    // Graphs (groups of screens)
    const val AUTH = "auth"
    const val MAIN = "main"

    // Access (no bottom tabs)
    const val START = "start"            // 01
    const val LOGIN = "login"            // 02
    const val SIGN_UP = "signup"         // 03

    // Bottom tabs
    const val NOTIFICATIONS = "notifications" // 04
    const val GROUPS = "groups"               // 05
    const val PROFILE = "profile"             // 25

    // Groups
    const val GROUP = "group/{groupId}"                   // 07
    const val CREATE_GROUP = "create-group"               // 08
    const val EDIT_GROUP = "group/{groupId}/edit"         // 09
    const val INVITE_PEOPLE = "group/{groupId}/invite"    // 12

    // Expenses
    const val ADD_EXPENSE = "group/{groupId}/add-expense" // 13 (+ overlays 14–17)
    const val EXPENSE = "expense/{expenseId}"             // 18
    const val EDIT_EXPENSE = "expense/{expenseId}/edit"   // 19 (+ overlays 14–17)

    // Balances
    const val BALANCES = "group/{groupId}/balances"                  // 20
    const val PAYMENT = "group/{groupId}/pay/{toUserId}/{mode}"      // 21 (mode=full) / 22 (mode=partial)
    const val PAYMENT_RECORDED = "group/{groupId}/paid/{paymentId}"  // 23

    // Profile & settings
    const val APP_SETTINGS = "app-settings"                   // 26
    const val NOTIFICATION_SETTINGS = "notification-settings" // 27
    const val ACCOUNT_SETTINGS = "account-settings"           // 28

    fun group(groupId: String) = "group/$groupId"
    fun editGroup(groupId: String) = "group/$groupId/edit"
    fun invitePeople(groupId: String) = "group/$groupId/invite"
    fun addExpense(groupId: String) = "group/$groupId/add-expense"
    fun expense(expenseId: String) = "expense/$expenseId"
    fun editExpense(expenseId: String) = "expense/$expenseId/edit"
    fun balances(groupId: String) = "group/$groupId/balances"
    fun payment(groupId: String, toUserId: String, partial: Boolean) =
        "group/$groupId/pay/$toUserId/${if (partial) "partial" else "full"}"
    fun paymentRecorded(groupId: String, paymentId: String) = "group/$groupId/paid/$paymentId"

    /** Screens where the bottom tab bar is hidden (access screens + focused forms). */
    val screensWithoutTabs = setOf(
        START, LOGIN, SIGN_UP,
        CREATE_GROUP, ADD_EXPENSE, EDIT_EXPENSE, PAYMENT, PAYMENT_RECORDED,
    )
}
