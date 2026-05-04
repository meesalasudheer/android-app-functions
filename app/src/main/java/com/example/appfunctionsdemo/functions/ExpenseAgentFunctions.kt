package com.example.appfunctionsdemo.functions

import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.service.AppFunction
import com.example.appfunctionsdemo.ExpenseGraph
import com.example.appfunctionsdemo.core.ledger.AddExpenseRequest
import com.example.appfunctionsdemo.core.ledger.AddExpenseResult

class ExpenseAgentFunctions {
    @AppFunction(isEnabled = true)
    suspend fun addSharedExpense(
        context: AppFunctionContext,
        description: String,
        amountCents: Long,
        paidBy: String,
        participants: List<String>
    ): AddSharedExpenseResponse = addSharedExpenseInternal(
        description = description,
        amountCents = amountCents,
        paidBy = paidBy,
        participants = participants
    )

    internal fun addSharedExpenseInternal(
        description: String,
        amountCents: Long,
        paidBy: String,
        participants: List<String>
    ): AddSharedExpenseResponse {
        val result = ExpenseGraph.ledger.addExpense(
            AddExpenseRequest(
                description = description,
                amountCents = amountCents,
                paidBy = paidBy,
                participants = participants
            )
        )

        return when (result) {
            is AddExpenseResult.Success -> AddSharedExpenseResponse(
                success = true,
                message = "Expense recorded.",
                expenseId = result.expense.id,
                balances = result.balances.map { MemberBalance(it.key, it.value) }
            )

            is AddExpenseResult.Error -> AddSharedExpenseResponse(
                success = false,
                message = result.message,
                expenseId = null,
                balances = emptyList()
            )
        }
    }

    @AppFunction(isEnabled = true)
    suspend fun listRecentExpenses(
        context: AppFunctionContext,
        limit: Int
    ): RecentExpensesResponse = listRecentExpensesInternal(limit)

    internal fun listRecentExpensesInternal(limit: Int): RecentExpensesResponse {
        val safeLimit = limit.coerceIn(1, 20)
        val items = ExpenseGraph.ledger.recentExpenses(safeLimit).map {
            ExpenseSummary(
                id = it.id,
                description = it.description,
                amountCents = it.amountCents,
                paidBy = it.paidBy,
                participants = it.participants
            )
        }

        return RecentExpensesResponse(items)
    }
}

@AppFunctionSerializable
data class AddSharedExpenseResponse(
    val success: Boolean,
    val message: String,
    val expenseId: Long?,
    val balances: List<MemberBalance>
)

@AppFunctionSerializable
data class RecentExpensesResponse(
    val expenses: List<ExpenseSummary>
)

@AppFunctionSerializable
data class ExpenseSummary(
    val id: Long,
    val description: String,
    val amountCents: Long,
    val paidBy: String,
    val participants: List<String>
)

@AppFunctionSerializable
data class MemberBalance(
    val member: String,
    val balanceCents: Long
)
