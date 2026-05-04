package com.example.appfunctionsdemo.core.ledger

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExpenseLedgerTest {
    @Test
    fun addExpense_returnsError_whenDescriptionBlank() {
        val ledger = ExpenseLedger { 1000L }

        val result = ledger.addExpense(
            AddExpenseRequest(
                description = "  ",
                amountCents = 1000,
                paidBy = "Alex",
                participants = listOf("Alex")
            )
        )

        assertThat(result).isEqualTo(AddExpenseResult.Error("Description is required."))
    }

    @Test
    fun addExpense_returnsError_whenAmountIsNotPositive() {
        val ledger = ExpenseLedger { 1000L }

        val result = ledger.addExpense(
            AddExpenseRequest(
                description = "Lunch",
                amountCents = 0,
                paidBy = "Alex",
                participants = listOf("Alex")
            )
        )

        assertThat(result).isEqualTo(AddExpenseResult.Error("Amount must be greater than zero."))
    }

    @Test
    fun addExpense_returnsError_whenParticipantsDontIncludePayer() {
        val ledger = ExpenseLedger { 1000L }

        val result = ledger.addExpense(
            AddExpenseRequest(
                description = "Taxi",
                amountCents = 2000,
                paidBy = "Alex",
                participants = listOf("Sam")
            )
        )

        assertThat(result).isEqualTo(AddExpenseResult.Error("Participants must include the payer."))
    }

    @Test
    fun addExpense_succeeds_andComputesBalancesWithRemainder() {
        var time = 0L
        val ledger = ExpenseLedger { ++time }

        val result = ledger.addExpense(
            AddExpenseRequest(
                description = "Dinner",
                amountCents = 1001,
                paidBy = "Alex",
                participants = listOf("Alex", "Sam")
            )
        )

        assertThat(result).isInstanceOf(AddExpenseResult.Success::class.java)
        val success = result as AddExpenseResult.Success
        assertThat(success.expense.id).isEqualTo(1L)
        assertThat(success.balances).containsExactly(
            "Alex", 500L,
            "Sam", -500L
        )
    }

    @Test
    fun recentExpenses_returnsNewestFirstAndRespectsLimit() {
        var time = 100L
        val ledger = ExpenseLedger { ++time }

        ledger.addExpense(AddExpenseRequest("First", 1200, "Alex", listOf("Alex", "Sam")))
        ledger.addExpense(AddExpenseRequest("Second", 1800, "Sam", listOf("Alex", "Sam")))

        val recentOne = ledger.recentExpenses(limit = 1)

        assertThat(recentOne).hasSize(1)
        assertThat(recentOne.first().description).isEqualTo("Second")
    }

    @Test
    fun recentExpenses_returnsEmpty_whenLimitNotPositive() {
        val ledger = ExpenseLedger { 1L }
        ledger.addExpense(AddExpenseRequest("Snack", 500, "Alex", listOf("Alex")))

        assertThat(ledger.recentExpenses(limit = 0)).isEmpty()
    }
}
