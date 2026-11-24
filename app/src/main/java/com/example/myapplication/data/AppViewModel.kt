package com.example.myapplication.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

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
    val payments = mutableStateListOf<Payment>().apply { addAll(elements = SampleData.payments) }
    val invites = mutableStateListOf<Invite>().apply { addAll(SampleData.invites) }
    val sentInvites = mutableStateListOf<SentInvite>().apply { addAll(SampleData.sentInvites) }
    val notifications = mutableStateListOf<AppNotification>().apply { addAll(SampleData.notifications) }
    var settings by mutableStateOf(Settings())
        private set

    /** Fake connection status. Toggle it in App settings to see the offline banners. */
    var isOnline by mutableStateOf(true)
        private set
    var lastSynced by mutableStateOf("9:38 AM")
        private set

    /** Changes saved on this device that still have to reach the server (NFR-01, 04). */
    val pendingRequests = mutableStateListOf<PendingRequest>()
    private val requestQueue = RequestQueue(pendingRequests)

    /** One-off feedback message shown as a snackbar (NFR-05, 06). Cleared once shown. */
    var feedback by mutableStateOf<String?>(null)
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
    fun groupTotal(groupId: String): Long = expensesIn(groupId).sumOf { it.amount }

    // ---------- Access (01–03, 25, 28) ----------
    fun logIn(email: String, password: String): Boolean = email.isNotBlank() && password.isNotBlank()

    fun signUp(name: String, email: String, password: String): Boolean {
        if (name.isBlank() || email.isBlank() || password.isBlank()) return false
        setAccount(name.trim(), email.trim())
        return true
    }

    fun updateAccount(name: String, email: String) {
        setAccount(name, email)
        sync("Account changes")
    }

    private fun setAccount(name: String, email: String) {
        currentUser = currentUser.copy(name = name, email = email)
        users[ME] = currentUser
    }

    fun saveSettings(newSettings: Settings) {
        settings = newSettings
        sync("Settings")
    }

    // ---------- Groups (05, 08, 09, 12) ----------
    fun createGroup(name: String, description: String): String {
        val id = newId("g")
        groups.add(Group(id, name.trim(), description.trim(), listOf(ME), adminId = ME))
        sync("New group")
        return id
    }

    fun updateGroup(id: String, name: String, description: String) {
        changeGroup(id) { it.copy(name = name.trim(), description = description.trim()) }
        sync("Group changes")
    }

    fun removeMember(groupId: String, userId: String) {
        changeGroup(groupId) { it.copy(memberIds = it.memberIds - userId) }
        sync("Member removal")
    }

    fun leaveGroup(groupId: String) {
        groups.removeAll { it.id == groupId }
        sync("Leaving group")
    }

    fun deleteGroup(groupId: String) {
        groups.removeAll { it.id == groupId }
        expenses.removeAll { it.groupId == groupId }
        payments.removeAll { it.groupId == groupId }
        sentInvites.removeAll { it.groupId == groupId }
        sync("Group deletion")
    }

    private fun changeGroup(id: String, change: (Group) -> Group) {
        val index = groups.indexOfFirst { it.id == id }
        if (index >= 0) groups[index] = change(groups[index])
    }

    fun sendInvite(groupId: String, email: String) {
        sentInvites.add(SentInvite(newId("si"), groupId, email.trim()))
        sync("Invite")
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
        sync("Invite response")
    }

    fun declineInvite(inviteId: String) {
        invites.removeAll { it.id == inviteId }
        notifications.removeAll { it.inviteId == inviteId }
        sync("Invite response")
    }

    // ---------- Expenses (13, 18, 19) ----------
    fun addExpense(
        groupId: String,
        description: String,
        amount: Long,
        paidById: String,
        splitBetween: List<String>,
        note: String,
        hasReceipt: Boolean,
    ): String {
        val id = newId("e")
        expenses.add(Expense(id, groupId, description, amount, paidById, splitBetween, note, hasReceipt, today()))
        sync("Expense")
        return id
    }

    fun updateExpense(expense: Expense) {
        val index = expenses.indexOfFirst { it.id == expense.id }
        if (index >= 0) expenses[index] = expense
        sync("Expense changes")
    }

    // ---------- Balances (07, 20–23) – the maths lives in DebtCalculation ----------
    fun paidBy(groupId: String, userId: String): Long = DebtCalculation.paidBy(userId, expensesIn(groupId))

    fun shareOf(groupId: String, userId: String): Long = DebtCalculation.shareOf(userId, expensesIn(groupId))

    /** Positive = others owe this person. Negative = this person owes others. */
    fun netBalance(groupId: String, userId: String): Long =
        DebtCalculation.netBalance(userId, expensesIn(groupId), paymentsIn(groupId))

    fun myNet(groupId: String): Long = netBalance(groupId, ME)

    /** Who should pay whom in this group – the transaction list. */
    fun debts(groupId: String): List<Debt> {
        val group = group(groupId) ?: return emptyList()
        return DebtCalculation.settleUp(DebtCalculation.netBalances(group.memberIds, expensesIn(groupId), paymentsIn(groupId)))
    }

    /** The most I can pay [toId] in this group: paying more than this is not allowed. */
    fun outstanding(groupId: String, toId: String): Long = DebtCalculation.outstanding(debts(groupId), ME, toId)

    /** Records my payment to [toId]. Returns null (and records nothing) if it is zero or more than I owe. */
    fun recordPayment(groupId: String, toId: String, amount: Long): String? {
        if (!DebtCalculation.isValidPayment(amount, outstanding(groupId, toId))) return null
        val id = newId("p")
        payments.add(Payment(id, groupId, ME, toId, amount, today()))
        sync("Payment")
        return id
    }

    fun allSettled(): Boolean = groups.all { myNet(it.id) == 0L }

    // ---------- Offline + retry (shared behavior, NFR-01–07) ----------

    /**
     * Fake server call. A real backend would make a network request here; for now a request
     * succeeds exactly when the (simulated) connection is up.
     */
    private fun sendToServer(@Suppress("UNUSED_PARAMETER") request: PendingRequest): Boolean = isOnline

    /** Sends a change that was already applied locally; if it fails it is queued for automatic retry. */
    private fun sync(description: String) {
        feedback = if (requestQueue.submit(description, ::sendToServer)) {
            "$description saved" // NFR-05
        } else {
            "$description couldn't be sent – saved on this device, will retry automatically" // NFR-06
        }
    }

    fun feedbackShown() {
        feedback = null
    }

    /** App settings → Simulate offline mode. Coming back online retries everything queued (NFR-01). */
    fun setConnection(online: Boolean) {
        isOnline = online
        if (!online) return
        lastSynced = timeNow()
        val synced = requestQueue.retryAll(::sendToServer)
        if (synced.isNotEmpty()) { // NFR-07: only when a failed request has now succeeded
            val count = if (synced.size == 1) "1 change" else "${synced.size} changes"
            notifications.add(
                0,
                AppNotification(
                    newId("n"),
                    NotificationType.SYNC,
                    "Sync succeeded",
                    "$count made while offline synced in the background: " + synced.joinToString { it.description } + ".",
                    "Just now",
                ),
            )
            feedback = "Back online – $count synced"
        }
    }
}
