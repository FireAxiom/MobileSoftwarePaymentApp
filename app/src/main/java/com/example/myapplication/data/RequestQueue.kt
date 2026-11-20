package com.example.myapplication.data

/** A change that has been saved on this device but not yet sent to the server. */
data class PendingRequest(val id: Int, val description: String)

/**
 * Reliability: retry of failed requests (NFR-01, 04, 06, 07).
 *
 * Every change is first applied to the local data (so the app keeps working offline), then
 * "sent to the server" through this queue. If sending fails, the request is kept here and
 * retried later by [retryAll], without the user doing anything.
 *
 * [items] is passed in so AppViewModel can use a Compose state list (screens redraw when it
 * changes) while unit tests can use a plain list.
 */
class RequestQueue(private val items: MutableList<PendingRequest> = mutableListOf()) {
    private var nextId = 1

    val pending: List<PendingRequest> get() = items.toList()

    /** Tries to send at once. Returns true if sent, false if it failed and was queued for retry. */
    fun submit(description: String, send: (PendingRequest) -> Boolean): Boolean {
        val request = PendingRequest(nextId++, description)
        if (send(request)) return true
        items += request
        return false
    }

    /**
     * Retries every queued request in the order they were made. Requests that succeed leave the
     * queue; requests that fail again stay for the next retry. Returns the ones that succeeded.
     */
    fun retryAll(send: (PendingRequest) -> Boolean): List<PendingRequest> {
        val succeeded = items.filter(send)
        items.removeAll(succeeded)
        return succeeded
    }
}
