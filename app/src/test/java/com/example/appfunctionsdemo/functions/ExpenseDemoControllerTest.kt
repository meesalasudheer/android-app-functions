package com.example.appfunctionsdemo.functions

import com.example.appfunctionsdemo.core.ledger.ExpenseLedger
import com.example.appfunctionsdemo.ui.ExpenseDemoController
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExpenseDemoControllerTest {
    @Test
    fun addExpense_returnsValidationErrorForBadAmount() {
        val controller = ExpenseDemoController(ExpenseLedger())

        val message = controller.addExpense(
            description = "Coffee",
            amountCents = 0,
            paidBy = "Alex",
            participants = "Alex"
        )

        assertThat(message).isEqualTo("Amount must be greater than zero.")
    }

    @Test
    fun addExpense_addsExpenseAndShowsInRecentExpenses() {
        val controller = ExpenseDemoController(ExpenseLedger { 100L })

        val message = controller.addExpense(
            description = "Snacks",
            amountCents = 250,
            paidBy = "Alex",
            participants = "Alex, Sam"
        )

        assertThat(message).isEqualTo("Added expense #1.")
        assertThat(controller.recentExpenses()).contains("#1 Snacks - 250 cents")
    }
}
