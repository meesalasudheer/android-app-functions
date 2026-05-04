package com.example.agentcaller

data class BalanceItem(
    val member: String,
    val balanceCents: Long
)

data class ExpenseItem(
    val id: Long,
    val description: String,
    val amountCents: Long,
    val paidBy: String,
    val participants: List<String>
)

data class AddExpenseCallResult(
    val success: Boolean,
    val message: String,
    val expenseId: Long?,
    val balances: List<BalanceItem>
)

data class ListExpensesCallResult(
    val message: String,
    val expenses: List<ExpenseItem>
)

object TargetAppFunctions {
    const val TARGET_PACKAGE = "com.example.appfunctionsdemo"
    const val ADD_SHARED_EXPENSE_ID = "com.example.appfunctionsdemo.functions.ExpenseAgentFunctions#addSharedExpense"
    const val LIST_RECENT_EXPENSES_ID = "com.example.appfunctionsdemo.functions.ExpenseAgentFunctions#listRecentExpenses"
}

interface AgentCaller {
    suspend fun addSharedExpense(
        description: String,
        amountCents: Long,
        paidBy: String,
        participants: List<String>
    ): AddExpenseCallResult

    suspend fun listRecentExpenses(limit: Int): ListExpensesCallResult
}
