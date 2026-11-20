package com.example.myapplication.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DebtCalculationTest {

    private val everyone = listOf(ME, "maya", "leo")

    private fun expense(amount: Long, paidBy: String, split: List<String> = everyone) =
        Expense("e$amount$paidBy", "g", "Test", amount, paidBy, split, date = "")

    private fun payment(from: String, to: String, amount: Long) = Payment("p$amount", "g", from, to, amount, "")

    /** The cabin example: Maya paid $90, Leo paid $60, split 3 ways → $50 each. */
    private val cabin = listOf(expense(9000, "maya"), expense(6000, "leo"))

    private fun debtsFor(expenses: List<Expense>, payments: List<Payment> = emptyList()) =
        DebtCalculation.settleUp(DebtCalculation.netBalances(everyone, expenses, payments))

    // ---------- Step 1: net balances ----------

    @Test
    fun netBalances_workedExample() {
        val nets = DebtCalculation.netBalances(everyone, cabin, emptyList())
        assertEquals(-5000L, nets[ME])
        assertEquals(4000L, nets["maya"])
        assertEquals(1000L, nets["leo"])
    }

    @Test
    fun netBalances_alwaysSumToZero_withSampleData() {
        SampleData.groups.forEach { group ->
            val expenses = SampleData.expenses.filter { it.groupId == group.id }
            val payments = SampleData.payments.filter { it.groupId == group.id }
            val nets = DebtCalculation.netBalances(group.memberIds, expenses, payments)
            assertEquals("Group ${group.id}", 0L, nets.values.sum())
        }
    }

    // ---------- Step 2: transaction list ----------

    @Test
    fun settleUp_workedExample() {
        assertEquals(
            listOf(Debt(ME, "maya", 4000), Debt(ME, "leo", 1000)),
            debtsFor(cabin),
        )
    }

    @Test
    fun settleUp_emptyGroup_hasNoDebts() {
        assertEquals(emptyList<Debt>(), debtsFor(emptyList()))
    }

    @Test
    fun settleUp_atMostPeopleMinusOneTransactions() {
        val people = listOf("a", "b", "c", "d", "e")
        val expenses = people.mapIndexed { i, p -> expense((i + 1) * 1234L, p, people) }
        val debts = DebtCalculation.settleUp(DebtCalculation.netBalances(people, expenses, emptyList()))
        assertTrue(debts.size <= people.size - 1)
    }

    @Test
    fun settleUp_tiesAreBrokenByUserId() {
        val debts = DebtCalculation.settleUp(mapOf("b" to 500L, "a" to 500L, "c" to -1000L))
        assertEquals(listOf(Debt("c", "a", 500), Debt("c", "b", 500)), debts)
    }

    // ---------- Step 3: payments ----------

    @Test
    fun partialPayment_reducesTheDebt() {
        val debts = debtsFor(cabin, listOf(payment(ME, "maya", 2500)))
        assertEquals(listOf(Debt(ME, "maya", 1500), Debt(ME, "leo", 1000)), debts)
    }

    @Test
    fun fullPayment_removesTheRow() {
        val debts = debtsFor(cabin, listOf(payment(ME, "maya", 4000)))
        assertEquals(listOf(Debt(ME, "leo", 1000)), debts)
    }

    @Test
    fun payingEveryone_settlesTheGroup() {
        val debts = debtsFor(cabin, listOf(payment(ME, "maya", 4000), payment(ME, "leo", 1000)))
        assertTrue(debts.isEmpty())
    }

    @Test
    fun isValidPayment_rejectsZeroNegativeAndOverpaying() {
        assertTrue(DebtCalculation.isValidPayment(4000, outstanding = 4000)) // full
        assertTrue(DebtCalculation.isValidPayment(1, outstanding = 4000)) // partial
        assertFalse(DebtCalculation.isValidPayment(4001, outstanding = 4000)) // overpay
        assertFalse(DebtCalculation.isValidPayment(0, outstanding = 4000))
        assertFalse(DebtCalculation.isValidPayment(-100, outstanding = 4000))
        assertFalse(DebtCalculation.isValidPayment(100, outstanding = 0)) // owe nothing
    }

    @Test
    fun outstanding_isZeroWhenThereIsNoDebt() {
        assertEquals(4000L, DebtCalculation.outstanding(debtsFor(cabin), ME, "maya"))
        assertEquals(0L, DebtCalculation.outstanding(debtsFor(cabin), "maya", ME))
    }

    // ---------- Splitting cents ----------

    @Test
    fun split_100Over3_addsUpExactly_andLeftoverGoesToLowestId() {
        val parts = DebtCalculation.split(10000, everyone) // sorted: leo, maya, me
        assertEquals(10000L, parts.values.sum())
        assertEquals(3334L, parts["leo"])
        assertEquals(3333L, parts["maya"])
        assertEquals(3333L, parts[ME])
    }

    @Test
    fun split_nobody_isEmpty() {
        assertTrue(DebtCalculation.split(10000, emptyList()).isEmpty())
    }

    @Test
    fun unevenSplit_stillSumsToZero() {
        val nets = DebtCalculation.netBalances(everyone, listOf(expense(10000, ME)), emptyList())
        assertEquals(0L, nets.values.sum())
        assertEquals(6667L, nets[ME]) // paid 100.00, own share 33.33
    }

    // ---------- Formatting ----------

    @Test
    fun parseCents_acceptsCommonFormats() {
        assertEquals(1200L, parseCents("12"))
        assertEquals(1250L, parseCents("12.5"))
        assertEquals(1250L, parseCents("12,50"))
        assertEquals(1205L, parseCents(" 12.05 "))
    }

    @Test
    fun parseCents_rejectsInvalidInput() {
        assertNull(parseCents(""))
        assertNull(parseCents("abc"))
        assertNull(parseCents("12.345"))
        assertNull(parseCents("-5"))
    }

    @Test
    fun money_formatsCents() {
        assertEquals("$90.00", money(9000))
        assertEquals("$0.05", money(5))
        assertEquals("-$5.00", money(-500))
    }

    @Test
    fun perPersonText_showsRangeWhenUneven() {
        assertEquals("$30.00 each", perPersonText(9000, 3))
        assertEquals("$33.33–$33.34 each", perPersonText(10000, 3))
    }
}
