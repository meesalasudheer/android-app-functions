package com.example.appfunctionsdemo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.example.appfunctionsdemo.ExpenseGraph
import com.example.appfunctionsdemo.core.ledger.AddExpenseRequest
import com.example.appfunctionsdemo.core.ledger.AddExpenseResult
import com.example.appfunctionsdemo.core.ledger.ExpenseLedger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppFunctionsDemoApp(controller: ExpenseDemoController = remember { ExpenseDemoController(ExpenseGraph.ledger) }) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Compose + AppFunctions Demo") })
        }
    ) { paddingValues ->
        DemoScreen(controller = controller, modifier = Modifier.padding(paddingValues))
    }
}

@Composable
private fun DemoScreen(controller: ExpenseDemoController, modifier: Modifier = Modifier) {
    var description by remember { mutableStateOf("") }
    var amountCents by remember { mutableStateOf("") }
    var paidBy by remember { mutableStateOf("Alex") }
    var participants by remember { mutableStateOf("Alex,Sam") }
    var statusMessage by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .semantics { testTag = "screen" },
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Try in UI, then invoke same logic via AppFunctions from agent apps.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        item {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "descriptionField" },
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "amountField" },
                value = amountCents,
                onValueChange = { amountCents = it },
                label = { Text("Amount in cents") },
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "payerField" },
                value = paidBy,
                onValueChange = { paidBy = it },
                label = { Text("Paid by") },
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "participantsField" },
                value = participants,
                onValueChange = { participants = it },
                label = { Text("Participants (comma separated)") },
                singleLine = true
            )
        }

        item {
            Button(
                onClick = {
                    val amount = amountCents.toLongOrNull()
                    if (amount == null) {
                        statusMessage = "Amount must be a whole number in cents."
                        return@Button
                    }
                    statusMessage = controller.addExpense(
                        description = description,
                        amountCents = amount,
                        paidBy = paidBy,
                        participants = participants
                    )
                    if (statusMessage.startsWith("Added")) {
                        description = ""
                        amountCents = ""
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "saveButton" }
            ) {
                Text("Save Expense")
            }
        }

        item {
            if (statusMessage.isNotBlank()) {
                Text(
                    text = statusMessage,
                    modifier = Modifier.semantics { testTag = "statusMessage" },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            Text("Recent Expenses", style = MaterialTheme.typography.titleMedium)
        }

        items(controller.recentExpenses()) { expenseLine ->
            Card {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(expenseLine, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

class ExpenseDemoController(
    private val ledger: ExpenseLedger
) {
    fun addExpense(description: String, amountCents: Long, paidBy: String, participants: String): String {
        val result = ledger.addExpense(
            AddExpenseRequest(
                description = description,
                amountCents = amountCents,
                paidBy = paidBy,
                participants = participants.split(',').map { it.trim() }
            )
        )

        return when (result) {
            is AddExpenseResult.Success -> "Added expense #${result.expense.id}."
            is AddExpenseResult.Error -> result.message
        }
    }

    fun recentExpenses(limit: Int = 5): List<String> {
        return ledger.recentExpenses(limit).map { expense ->
            "#${expense.id} ${expense.description} - ${expense.amountCents} cents"
        }
    }
}
