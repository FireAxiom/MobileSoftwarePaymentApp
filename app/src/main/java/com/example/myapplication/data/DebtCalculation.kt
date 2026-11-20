package com.example.myapplication.data

/**
 * Who owes whom in one group. Pure functions, no state – AppViewModel passes in the group's
 * expenses and payments and shows the result.
 *
 * All amounts are whole cents (Long), so every sum is exact: $90.00 is 9000.
 *
 * The algorithm, per group:
 *  1. Net balance per person = paid − share + payments sent − payments received.
 *     Positive = others owe them, negative = they owe others. All nets add up to 0.
 *  2. Settle up: the biggest debtor pays the biggest creditor min(debt, credit). Repeat until
 *     everyone is at 0. This gives at most (people − 1) transactions.
 *  3. Debts are never stored. After every payment the list is simply calculated again, so a
 *     partial payment shrinks a debt and a full payment removes it.
 *
 * Ties and leftover cents are resolved by user ID (sorted), so the result never depends on
 * names, list order or the device.
 */
object DebtCalculation {

    /**
     * Splits [amount] equally between [people]. Leftover cents go one each to the lowest user IDs,
     * so the parts always add up to exactly [amount]: $100.00 / 3 → 33.34 + 33.33 + 33.33.
     */
    fun split(amount: Long, people: List<String>): Map<String, Long> {
        val ids = people.distinct().sorted()
        if (ids.isEmpty()) return emptyMap()
        val base = amount / ids.size
        val leftover = amount % ids.size
        return ids.withIndex().associate { (index, id) -> id to base + if (index < leftover) 1 else 0 }
    }

    /** Total that [userId] paid for expenses. */
    fun paidBy(userId: String, expenses: List<Expense>): Long =
        expenses.filter { it.paidById == userId }.sumOf { it.amount }

    /** [userId]'s part of all expenses they are split into. */
    fun shareOf(userId: String, expenses: List<Expense>): Long =
        expenses.sumOf { split(it.amount, it.splitBetween)[userId] ?: 0L }

    /** Positive = others owe this person. Negative = this person owes others. */
    fun netBalance(userId: String, expenses: List<Expense>, payments: List<Payment>): Long {
        val sent = payments.filter { it.fromId == userId }.sumOf { it.amount }
        val received = payments.filter { it.toId == userId }.sumOf { it.amount }
        return paidBy(userId, expenses) - shareOf(userId, expenses) + sent - received
    }

    /** Net balance of everyone involved: [members] plus anyone who appears in an expense or payment. */
    fun netBalances(members: List<String>, expenses: List<Expense>, payments: List<Payment>): Map<String, Long> {
        val people = members +
            expenses.flatMap { it.splitBetween + it.paidById } +
            payments.flatMap { listOf(it.fromId, it.toId) }
        return people.distinct().sorted().associateWith { netBalance(it, expenses, payments) }
    }

    /** Step 2: turns net balances into the shortest clear list of "X pays Y amount". */
    fun settleUp(balances: Map<String, Long>): List<Debt> {
        // Sorted by user ID: on a tie, minBy/maxBy pick the first one, i.e. the lowest ID.
        val remaining = balances.toSortedMap()
        val result = mutableListOf<Debt>()
        while (true) {
            val (debtor, owes) = remaining.entries.minByOrNull { it.value }?.toPair() ?: break
            val (creditor, owed) = remaining.entries.maxByOrNull { it.value }?.toPair() ?: break
            if (owes >= 0 || owed <= 0) break
            val amount = minOf(-owes, owed)
            result += Debt(debtor, creditor, amount)
            remaining[debtor] = owes + amount
            remaining[creditor] = owed - amount
        }
        return result
    }

    /** How much [fromId] still has to pay [toId] – the most they can pay without overpaying. */
    fun outstanding(debts: List<Debt>, fromId: String, toId: String): Long =
        debts.find { it.fromId == fromId && it.toId == toId }?.amount ?: 0L

    /** Full payment = exactly [outstanding]; partial = less. Zero, negative or overpaying is rejected. */
    fun isValidPayment(amount: Long, outstanding: Long): Boolean = amount in 1..outstanding
}
