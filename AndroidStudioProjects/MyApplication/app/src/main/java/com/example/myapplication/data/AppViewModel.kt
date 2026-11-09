package com.example.myapplication.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.math.abs

/**
 * The app's single source of data. For now it is a FAKE backend: everything lives in memory
 * and resets when the app restarts. Screens read the lists below and call the functions to
 * change them; because they are Compose "state", screens redraw automatically.
 *
 * Later, the insides of these functions can be swapped for real API / database calls.
 */
class AppViewModel : ViewModel() {

    // ---------- State ----------
    var currentUser by mutableStateOf(SampleData.me)
        private set

    private val users = mutableStateMapOf<String, User>().apply { SampleData.users.forEach { put(it.id, it) } }
    val groups = mutableStateListOf<Group>().apply { addAll(SampleData.groups) }
    val expenses = mutableStateListOf<Expense>().apply { addAll(SampleData.expenses) }
    val payments = mutableStateListOf<Payment>()
    val invites = mutableStateListOf<Invite>().apply { addAll(SampleData.invites) }
    val sentInvites = mutableStateListOf<SentInvite>().apply { addAll(SampleData.sentInvites) }
    val notifications = mutableStateListOf<AppNotification>().apply { addAll(SampleData.notifications) }
    var settings by mutableStateOf(Settings())

    /** Fake connection status. Toggle it in App settings to see the offline banners. */
    var isOnline by mutableStateOf(true)
        private set
    var lastSynced by mutableStateOf("9:38 AM")
        private set

    private var nextId = 100
    private fun newId(prefix: String) = "$prefix${nextId++}"

    // ---------- Lookups ----------
    fun user(id: String): User = users[id] ?: User(id, id, "")
    fun displayName(id: String): String = if (id == ME) "You" else user(id).name
    fun firstName(id: String): String = if (id == ME) "You" else user(id).name.substringBefore(" ")
    fun group(id: String): Group? = groups.find { it.id == id }
    fun membersOf(group: Group): List<User> = group.memberIds.map { user(it) }
    fun expense(id: String): Expense? = expenses.find { it.id == id }
    fun expensesIn(groupId: String): List<Expense> = expenses.filter { it.groupId == groupId }
    fun payment(id: String): Payment? = payments.find { it.id == id }
    fun paymentsIn(groupId: String): List<Payment> = payments.filter { it.groupId == groupId }
    fun invite(id: String): Invite? = invites.find { it.id == id }
    fun sentInvitesFor(groupId: String): List<SentInvite> = sentInvites.filter { it.groupId == groupId }
    fun groupTotal(groupId: String): Double = expensesIn(groupId).sumOf { it.amount }

    // ---------- Access (01–03, 25, 28) ----------
    fun logIn(email: String, password: String): Boolean = email.isNotBlank() && password.isNotBlank()

    fun signUp(name: String, email: String, password: String): Boolean {
        if (name.isBlank() || email.isBlank() || password.isBlank()) return false
        updateAccount(name.trim(), email.trim())
        return true
    }

    fun updateAccount(name: String, email: String) {
        currentUser = currentUser.copy(name = name, email = email)
        users[ME] = currentUser
    }

    // ---------- Groups (05, 08, 09, 12) ----------
    fun createGroup(name: String, description: String): String {
        val id = newId("g")
        groups.add(Group(id, name.trim(), description.trim(), listOf(ME), adminId = ME))
        return id
    }

    fun updateGroup(id: String, name: String, description: String) =
        changeGroup(id) { it.copy(name = name.trim(), description = description.trim()) }

    fun removeMember(groupId: String, userId: String) =
        changeGroup(groupId) { it.copy(memberIds = it.memberIds - userId) }

    fun leaveGroup(groupId: String) {
        groups.removeAll { it.id == groupId }
    }

    fun deleteGroup(groupId: String) {
        groups.removeAll { it.id == groupId }
        expenses.removeAll { it.groupId == groupId }
        payments.removeAll { it.groupId == groupId }
        sentInvites.removeAll { it.groupId == groupId }
    }

    private fun changeGroup(id: String, change: (Group) -> Group) {
        val index = groups.indexOfFirst { it.id == id }
        if (index >= 0) groups[index] = change(groups[index])
    }

    fun sendInvite(groupId: String, email: String) {
        sentInvites.add(SentInvite(newId("si"), groupId, email.trim()))
    }

    // ---------- Invitations I received (04, 10) ----------
    fun acceptInvite(inviteId: String) {
        val invite = invite(inviteId) ?: return
        val groupId = newId("g")
        groups.add(Group(groupId, invite.groupName, invite.description, invite.memberIds + ME, adminId = invite.fromUserId))
        invites.remove(invite)
        notifications.removeAll { it.inviteId == inviteId }
        notifications.add(
            0,
            AppNotification(newId("n"), NotificationType.ACTIVITY, "You joined ${invite.groupName}", "Find it in Groups", "Just now", groupId = groupId),
        )
    }

    fun declineInvite(inviteId: String) {
        invites.removeAll { it.id == inviteId }
        notifications.removeAll { it.inviteId == inviteId }
    }

    // ---------- Expenses (13, 18, 19) ----------
    fun addExpense(
        groupId: String,
        description: String,
        amount: Double,
        paidById: String,
        splitBetween: List<String>,
        note: String,
        hasReceipt: Boolean,
    ): String {
        val id = newId("e")
        expenses.add(Expense(id, groupId, description, round2(amount), paidById, splitBetween, note, hasReceipt, today()))
        return id
    }

    fun updateExpense(expense: Expense) {
        val index = expenses.indexOfFirst { it.id == expense.id }
        if (index >= 0) expenses[index] = expense
    }

    // ---------- Balances (07, 20–23) ----------
    fun paidBy(groupId: String, userId: String): Double =
        expensesIn(groupId).filter { it.paidById == userId }.sumOf { it.amount }

    fun shareOf(groupId: String, userId: String): Double =
        expensesIn(groupId).filter { userId in it.splitBetween }.sumOf { it.amount / it.splitBetween.size }

    /** Positive = others owe this person. Negative = this person owes others. */
    fun netBalance(groupId: String, userId: String): Double {
        val sent = paymentsIn(groupId).filter { it.fromId == userId }.sumOf { it.amount }
        val received = paymentsIn(groupId).filter { it.toId == userId }.sumOf { it.amount }
        return round2(paidBy(groupId, userId) - shareOf(groupId, userId) + sent - received)
    }

    fun myNet(groupId: String): Double = netBalance(groupId, ME)

    /** Who should pay whom: biggest debtor pays biggest creditor, repeat. */
    fun debts(groupId: String): List<Debt> {
        val group = group(groupId) ?: return emptyList()
        val people = (group.memberIds + expensesIn(groupId).flatMap { it.splitBetween + it.paidById }).distinct()
        val balance = people.associateWith { netBalance(groupId, it) }.toMutableMap()
        val result = mutableListOf<Debt>()
        while (true) {
            val (debtor, owes) = balance.minByOrNull { it.value }?.toPair() ?: break
            val (creditor, owed) = balance.maxByOrNull { it.value }?.toPair() ?: break
            if (owes > -0.005 || owed < 0.005) break
            val amount = round2(minOf(-owes, owed))
            result += Debt(debtor, creditor, amount)
            balance[debtor] = round2(owes + amount)
            balance[creditor] = round2(owed - amount)
        }
        return result
    }

    fun recordPayment(groupId: String, toId: String, amount: Double): String {
        val id = newId("p")
        payments.add(Payment(id, groupId, ME, toId, round2(amount), today()))
        return id
    }

    fun allSettled(): Boolean = groups.all { abs(myNet(it.id)) < 0.005 }

    // ---------- Offline (shared behavior) ----------
    fun setConnection(online: Boolean) {
        if (online && !isOnline) {
            lastSynced = timeNow()
            notifications.add(
                0,
                AppNotification(newId("n"), NotificationType.SYNC, "Sync succeeded", "Changes made while offline were synced in the background.", "Just now"),
            )
        }
        isOnline = online
    }
}
