package com.example.agentcaller.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.example.agentcaller.AgentCaller
import com.example.agentcaller.ExpenseItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentCallerApp(agentCaller: AgentCaller) {
    val scope = rememberCoroutineScope()

    var description by remember { mutableStateOf("Cab ride") }
    var amountInput by remember { mutableStateOf("1250") }
    var paidBy by remember { mutableStateOf("Alex") }
    var participants by remember { mutableStateOf("Alex,Sam") }
    var status by remember { mutableStateOf("Ready to call AppFunctions") }
    var expenses by remember { mutableStateOf(emptyList<ExpenseItem>()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Agent Caller (AppFunctions)") }) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "Targets package: com.example.appfunctionsdemo",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Amount (cents)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = paidBy,
                    onValueChange = { paidBy = it },
                    label = { Text("Paid by") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = participants,
                    onValueChange = { participants = it },
                    label = { Text("Participants (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Button(
                    onClick = {
                        val amountCents = amountInput.toLongOrNull()
                        if (amountCents == null) {
                            status = "Amount must be numeric."
                            return@Button
                        }

                        scope.launch {
                            val result = agentCaller.addSharedExpense(
                                description = description,
                                amountCents = amountCents,
                                paidBy = paidBy,
                                participants = participants.split(',').map { it.trim() }.filter { it.isNotBlank() }
                            )
                            status = "addSharedExpense -> ${result.message}"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().semantics { testTag = "invokeAddButton" }
                ) {
                    Text("Invoke addSharedExpense")
                }
            }

            item {
                Button(
                    onClick = {
                        scope.launch {
                            val result = agentCaller.listRecentExpenses(limit = 10)
                            expenses = result.expenses
                            status = "listRecentExpenses -> ${result.message}"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().semantics { testTag = "invokeListButton" }
                ) {
                    Text("Invoke listRecentExpenses")
                }
            }

            item {
                Text(
                    text = status,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics { testTag = "statusText" }
                )
            }

            item {
                Text(text = "Fetched expenses", style = MaterialTheme.typography.titleMedium)
            }

            items(expenses) { expense ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("#${expense.id} ${expense.description}")
                        Text("${expense.amountCents} cents paid by ${expense.paidBy}")
                        Text("Participants: ${expense.participants.joinToString()}")
                    }
                }
            }
        }
    }
}
