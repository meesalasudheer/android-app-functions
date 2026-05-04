package com.example.appfunctionsdemo.core.ledger

data class LedgerExpense(
    val id: Long,
    val description: String,
    val amountCents: Long,
    val paidBy: String,
    val participants: List<String>,
    val createdAtMillis: Long
)

data class AddExpenseRequest(
    val description: String,
    val amountCents: Long,
    val paidBy: String,
    val participants: List<String>
)

sealed interface AddExpenseResult {
    data class Success(val expense: LedgerExpense, val balances: Map<String, Long>) : AddExpenseResult
    data class Error(val message: String) : AddExpenseResult
}

class ExpenseLedger(
    private val nowMillis: () -> Long = { System.currentTimeMillis() }
) {
    private val expenses = mutableListOf<LedgerExpense>()
    private var nextExpenseId = 1L

    fun addExpense(request: AddExpenseRequest): AddExpenseResult {
        val description = request.description.trim()
        if (description.isBlank()) {
            return AddExpenseResult.Error("Description is required.")
        }
        if (request.amountCents <= 0L) {
            return AddExpenseResult.Error("Amount must be greater than zero.")
        }

        val payer = request.paidBy.trim()
        if (payer.isBlank()) {
            return AddExpenseResult.Error("Payer is required.")
        }

        val participants = request.participants.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        if (participants.isEmpty()) {
            return AddExpenseResult.Error("At least one participant is required.")
        }
        if (payer !in participants) {
            return AddExpenseResult.Error("Participants must include the payer.")
        }

        val expense = LedgerExpense(
            id = nextExpenseId++,
            description = description,
            amountCents = request.amountCents,
            paidBy = payer,
            participants = participants,
            createdAtMillis = nowMillis()
        )
        expenses += expense
        return AddExpenseResult.Success(expense = expense, balances = currentBalances())
    }

    fun recentExpenses(limit: Int = 10): List<LedgerExpense> {
        if (limit <= 0) return emptyList()
        return expenses
            .sortedByDescending { it.createdAtMillis }
            .take(limit)
    }

    fun currentBalances(): Map<String, Long> {
        val balances = mutableMapOf<String, Long>()

        for (expense in expenses) {
            val splitAmount = expense.amountCents / expense.participants.size
            val remainder = expense.amountCents % expense.participants.size

            expense.participants.forEachIndexed { index, person ->
                val share = splitAmount + if (index < remainder) 1 else 0
                balances[person] = (balances[person] ?: 0L) - share
            }
            balances[expense.paidBy] = (balances[expense.paidBy] ?: 0L) + expense.amountCents
        }

        return balances.toSortedMap()
    }
}
