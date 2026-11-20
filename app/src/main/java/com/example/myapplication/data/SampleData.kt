package com.example.myapplication.data

import com.example.myapplication.data.NotificationType.ACTIVITY
import com.example.myapplication.data.NotificationType.INVITE

/** Fake starting data, taken from the examples in the lo-fi wireframes. */
object SampleData {
    val me = User(ME, "Alex Morgan", "alex.morgan@email.com")

    val users = listOf(
        me,
        User("maya", "Maya Chen", "maya.chen@email.com"),
        User("leo", "Leo Park", "leo.park@email.com"),
        User("sam", "Sam Rivera", "sam.rivera@email.com"),
    )

    val groups = listOf(
        Group("g1", "Cabin weekend", "October trip to the lake", listOf(ME, "maya", "leo"), adminId = ME),
        Group("g2", "Friday dinner", "Our monthly catch-up", listOf(ME, "maya", "leo"), adminId = "maya"),
    )

    private val everyone = listOf(ME, "maya", "leo")

    val expenses = listOf(
        Expense("e1", "g1", "Groceries", 9000, "maya", everyone, note = "Food for the weekend", hasReceipt = true, date = "Oct 16, 2026"),
        Expense("e2", "g1", "Cabin booking", 18000, "maya", everyone, date = "Oct 15, 2026"),
        Expense("e3", "g1", "Fuel", 9000, "leo", everyone, date = "Oct 17, 2026"),
        Expense("e4", "g2", "Dinner", 9600, ME, everyone, date = "Oct 18, 2026"),
    )

    val invites = listOf(
        Invite("i1", "Sunday hike", "Join Maya and Sam to split the day's expenses.", "maya", listOf("maya", "sam")),
    )

    val sentInvites = listOf(SentInvite("si1", "g1", "sam.rivera@email.com"))

    val notifications = listOf(
        AppNotification("n1", INVITE, "Maya invited you to Sunday hike", "Join Maya and Sam to split the day's expenses.", "Today", inviteId = "i1"),
        AppNotification("n2", ACTIVITY, "Maya added Cabin booking", "Cabin weekend · $180.00", "3 hours ago", groupId = "g1", expenseId = "e2"),
        AppNotification("n3", ACTIVITY, "Leo added Fuel", "Cabin weekend · $90.00", "Yesterday", groupId = "g1", expenseId = "e3"),
        AppNotification("n4", ACTIVITY, "You added Dinner", "Friday dinner · $96.00", "Earlier", groupId = "g2", expenseId = "e4"),
    )

    val payments = listOf(
        Payment("p1", groupId = "g1", fromId = "maya", toId = "leo", amount = 10000, date = "Oct 30, 2026")
    )
}
