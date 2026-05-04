package com.example.appfunctionsdemo.functions

import com.example.appfunctionsdemo.ExpenseGraph
import com.example.appfunctionsdemo.core.ledger.ExpenseLedger
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ExpenseAgentFunctionsTest {
    private lateinit var functions: ExpenseAgentFunctions

    @Before
    fun setUp() {
        ExpenseGraph.ledger = ExpenseLedger { 1000L }
        functions = ExpenseAgentFunctions()
    }

    @Test
    fun addSharedExpense_successReturnsExpenseAndBalances() = runTest {
        val response = functions.addSharedExpenseInternal(
            description = "Dinner",
            amountCents = 1000,
            paidBy = "Alex",
            participants = listOf("Alex", "Sam")
        )

        assertThat(response.success).isTrue()
        assertThat(response.expenseId).isEqualTo(1L)
        assertThat(response.balances).hasSize(2)
    }

    @Test
    fun addSharedExpense_errorReturnsFailureMessage() = runTest {
        val response = functions.addSharedExpenseInternal(
            description = "",
            amountCents = 1000,
            paidBy = "Alex",
            participants = listOf("Alex")
        )

        assertThat(response.success).isFalse()
        assertThat(response.message).isEqualTo("Description is required.")
    }

    @Test
    fun listRecentExpenses_capsLimitAndReturnsItems() = runTest {
        functions.addSharedExpenseInternal("Cab", 1200, "Alex", listOf("Alex", "Sam"))
        functions.addSharedExpenseInternal("Lunch", 1500, "Sam", listOf("Alex", "Sam"))

        val response = functions.listRecentExpensesInternal(100)

        assertThat(response.expenses).hasSize(2)
        assertThat(response.expenses.first().description).isEqualTo("Cab")
    }
}
