package com.example.myapplication.data

/** The id of the logged-in user. Everything is shown from "my" point of view. */
const val ME = "me"

data class User(val id: String, val name: String, val email: String)

data class Group(
    val id: String,
    val name: String,
    val description: String,
    val memberIds: List<String>,
    val adminId: String,
)

data class Expense(
    val id: String,
    val groupId: String,
    val description: String,
    val amount: Double,
    val paidById: String,
    val splitBetween: List<String>, // split equally between these people
    val note: String = "",
    val hasReceipt: Boolean = false,
    val date: String,
)

/** A payment that was made outside the app and is only recorded here. */
data class Payment(
    val id: String,
    val groupId: String,
    val fromId: String,
    val toId: String,
    val amount: Double,
    val date: String,
)

/** "fromId owes toId this amount" – calculated, never stored. */
data class Debt(val fromId: String, val toId: String, val amount: Double)

/** An invitation someone sent to me (shown in Notifications, screen 10). */
data class Invite(
    val id: String,
    val groupName: String,
    val description: String,
    val fromUserId: String,
    val memberIds: List<String>,
)

/** An invitation I sent to someone (shown as "Pending" on screen 12). */
data class SentInvite(val id: String, val groupId: String, val email: String)

enum class NotificationType { INVITE, ACTIVITY, SYNC }

data class AppNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val time: String,
    val groupId: String? = null,
    val expenseId: String? = null,
    val inviteId: String? = null,
)

data class Settings(
    val currency: String = "USD",
    val language: String = "English",
    val dateFormat: String = "DD/MM/YYYY",
    val pushNotifications: Boolean = true,
    val emailNotifications: Boolean = false,
    val notifyInvites: Boolean = true,
    val notifyExpenses: Boolean = true,
    val notifyPayments: Boolean = true,
)
