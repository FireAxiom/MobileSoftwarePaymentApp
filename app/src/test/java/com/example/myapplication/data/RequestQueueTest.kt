package com.example.myapplication.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Reliability QA: failed requests are kept and retried automatically (NFR-01, 04, 06, 07). */
class RequestQueueTest {

    private val online: (PendingRequest) -> Boolean = { true }
    private val offline: (PendingRequest) -> Boolean = { false }

    // ---------- RequestQueue on its own ----------

    @Test
    fun submit_online_isSentAndNotQueued() {
        val queue = RequestQueue()
        assertTrue(queue.submit("Expense", online))
        assertTrue(queue.pending.isEmpty())
    }

    @Test
    fun submit_offline_failsAndIsQueued() {
        val queue = RequestQueue()
        assertFalse(queue.submit("Expense", offline))
        assertEquals(listOf("Expense"), queue.pending.map { it.description })
    }

    @Test
    fun retryAll_whenBackOnline_sendsEverythingInOrderAndEmptiesQueue() {
        val queue = RequestQueue()
        queue.submit("Expense", offline)
        queue.submit("Payment", offline)
        val sent = mutableListOf<String>()

        val succeeded = queue.retryAll { sent += it.description; true }

        assertEquals(listOf("Expense", "Payment"), sent)
        assertEquals(2, succeeded.size)
        assertTrue(queue.pending.isEmpty())
    }

    @Test
    fun retryAll_stillFailing_keepsRequestsForNextRetry() {
        val queue = RequestQueue()
        queue.submit("Expense", offline)

        assertTrue(queue.retryAll(offline).isEmpty())
        assertEquals(1, queue.pending.size)
    }

    @Test
    fun retryAll_partialFailure_keepsOnlyTheFailedOnes() {
        val queue = RequestQueue()
        queue.submit("Expense", offline)
        queue.submit("Payment", offline)

        queue.retryAll { it.description == "Expense" }

        assertEquals(listOf("Payment"), queue.pending.map { it.description })
    }

    // ---------- Whole flow through AppViewModel (offline toggle = fault injection) ----------

    private fun addLunch(vm: AppViewModel) =
        vm.addExpense("g1", "Lunch", 3000, ME, listOf(ME, "maya"), note = "", hasReceipt = false)

    @Test
    fun online_change_isSentAtOnce_andShowsSuccess() {
        val vm = AppViewModel()
        addLunch(vm)
        assertTrue(vm.pendingRequests.isEmpty())
        assertEquals("Expense saved", vm.feedback) // NFR-05
    }

    @Test
    fun offline_change_staysUsable_isPending_andShowsFailure() {
        val vm = AppViewModel()
        vm.setConnection(online = false)

        val id = addLunch(vm)

        assertTrue(vm.expense(id) != null) // NFR-02/03: data still usable locally
        assertEquals(1, vm.pendingRequests.size) // NFR-04
        assertTrue(vm.feedback!!.contains("will retry")) // NFR-06
    }

    @Test
    fun reconnecting_retriesAutomatically_andNotifiesUser() {
        val vm = AppViewModel()
        vm.setConnection(online = false)
        addLunch(vm)
        val notificationsBefore = vm.notifications.size

        vm.setConnection(online = true) // no user action besides the network coming back

        assertTrue(vm.pendingRequests.isEmpty()) // NFR-01
        assertEquals(notificationsBefore + 1, vm.notifications.size) // NFR-07
        assertEquals(NotificationType.SYNC, vm.notifications.first().type)
    }

    @Test
    fun reconnecting_withNothingPending_doesNotNotify() {
        val vm = AppViewModel()
        vm.setConnection(online = false)
        val notificationsBefore = vm.notifications.size

        vm.setConnection(online = true)

        assertEquals(notificationsBefore, vm.notifications.size)
    }
}
