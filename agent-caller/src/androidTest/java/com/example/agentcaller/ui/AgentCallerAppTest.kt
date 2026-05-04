package com.example.agentcaller.ui

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.agentcaller.AddExpenseCallResult
import com.example.agentcaller.AgentCaller
import com.example.agentcaller.BalanceItem
import com.example.agentcaller.ExpenseItem
import com.example.agentcaller.ListExpensesCallResult
import org.junit.Rule
import org.junit.Test

class AgentCallerAppTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun invokeButtons_updatesStatusAndRendersExpenses() {
        val fakeCaller = object : AgentCaller {
            override suspend fun addSharedExpense(
                description: String,
                amountCents: Long,
                paidBy: String,
                participants: List<String>
            ): AddExpenseCallResult {
                return AddExpenseCallResult(
                    success = true,
                    message = "Fake add success",
                    expenseId = 42L,
                    balances = listOf(BalanceItem(member = "Alex", balanceCents = 500L))
                )
            }

            override suspend fun listRecentExpenses(limit: Int): ListExpensesCallResult {
                return ListExpensesCallResult(
                    message = "Fake list success",
                    expenses = listOf(
                        ExpenseItem(
                            id = 42L,
                            description = "Fake expense",
                            amountCents = 1200L,
                            paidBy = "Alex",
                            participants = listOf("Alex", "Sam")
                        )
                    )
                )
            }
        }

        composeRule.setContent {
            AgentCallerApp(agentCaller = fakeCaller)
        }

        composeRule.onNodeWithTag("invokeAddButton").performClick()
        composeRule.onNodeWithTag("statusText").assertTextContains("addSharedExpense -> Fake add success")

        composeRule.onNodeWithTag("invokeListButton").performClick()
        composeRule.onNodeWithTag("statusText").assertTextContains("listRecentExpenses -> Fake list success")
        composeRule.onNodeWithText("#42 Fake expense").assertTextContains("Fake expense")
    }
}
