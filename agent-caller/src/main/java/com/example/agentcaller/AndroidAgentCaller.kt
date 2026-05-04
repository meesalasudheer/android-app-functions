package com.example.agentcaller

import android.content.Context
import android.os.Build
import androidx.appfunctions.AppFunctionData
import androidx.appfunctions.AppFunctionManager
import androidx.appfunctions.AppFunctionSearchSpec
import androidx.appfunctions.ExecuteAppFunctionRequest
import androidx.appfunctions.ExecuteAppFunctionResponse
import androidx.appfunctions.metadata.AppFunctionMetadata
import kotlinx.coroutines.flow.first

class AndroidAgentCaller(
    context: Context,
    private val targetPackage: String = TargetAppFunctions.TARGET_PACKAGE
) : AgentCaller {

    private val appContext = context.applicationContext

    override suspend fun addSharedExpense(
        description: String,
        amountCents: Long,
        paidBy: String,
        participants: List<String>
    ): AddExpenseCallResult {
        if (Build.VERSION.SDK_INT < 36) {
            return AddExpenseCallResult(
                success = false,
                message = "AppFunctions caller requires Android 16+ (API 36).",
                expenseId = null,
                balances = emptyList()
            )
        }

        val manager = AppFunctionManager.getInstance(appContext)
            ?: return AddExpenseCallResult(
                success = false,
                message = "AppFunctionManager not available on this device.",
                expenseId = null,
                balances = emptyList()
            )

        return try {
            val metadata = findFunctionMetadata(
                manager = manager,
                functionId = TargetAppFunctions.ADD_SHARED_EXPENSE_ID
            ) ?: return AddExpenseCallResult(
                success = false,
                message = "Target function metadata not found. Is the provider app installed/indexed?",
                expenseId = null,
                balances = emptyList()
            )

            val params = AppFunctionData.Builder(metadata.parameters, metadata.components)
                .setString("description", description)
                .setLong("amountCents", amountCents)
                .setString("paidBy", paidBy)
                .setStringList("participants", participants)
                .build()

            val request = ExecuteAppFunctionRequest(
                targetPackageName = targetPackage,
                functionIdentifier = TargetAppFunctions.ADD_SHARED_EXPENSE_ID,
                functionParameters = params
            )

            when (val response = manager.executeAppFunction(request)) {
                is ExecuteAppFunctionResponse.Success -> parseAddExpenseSuccess(response)
                is ExecuteAppFunctionResponse.Error -> AddExpenseCallResult(
                    success = false,
                    message = response.error.toString(),
                    expenseId = null,
                    balances = emptyList()
                )
            }
        } catch (error: SecurityException) {
            AddExpenseCallResult(
                success = false,
                message = "Caller lacks EXECUTE_APP_FUNCTIONS permission: ${error.message}",
                expenseId = null,
                balances = emptyList()
            )
        } catch (error: Throwable) {
            AddExpenseCallResult(
                success = false,
                message = "Call failed: ${error.message}",
                expenseId = null,
                balances = emptyList()
            )
        }
    }

    override suspend fun listRecentExpenses(limit: Int): ListExpensesCallResult {
        if (Build.VERSION.SDK_INT < 36) {
            return ListExpensesCallResult(
                message = "AppFunctions caller requires Android 16+ (API 36).",
                expenses = emptyList()
            )
        }

        val manager = AppFunctionManager.getInstance(appContext)
            ?: return ListExpensesCallResult(
                message = "AppFunctionManager not available on this device.",
                expenses = emptyList()
            )

        return try {
            val metadata = findFunctionMetadata(
                manager = manager,
                functionId = TargetAppFunctions.LIST_RECENT_EXPENSES_ID
            ) ?: return ListExpensesCallResult(
                message = "Target function metadata not found. Is the provider app installed/indexed?",
                expenses = emptyList()
            )

            val params = AppFunctionData.Builder(metadata.parameters, metadata.components)
                .setInt("limit", limit)
                .build()

            val request = ExecuteAppFunctionRequest(
                targetPackageName = targetPackage,
                functionIdentifier = TargetAppFunctions.LIST_RECENT_EXPENSES_ID,
                functionParameters = params
            )

            when (val response = manager.executeAppFunction(request)) {
                is ExecuteAppFunctionResponse.Success -> parseListExpensesSuccess(response)
                is ExecuteAppFunctionResponse.Error -> ListExpensesCallResult(
                    message = response.error.toString(),
                    expenses = emptyList()
                )
            }
        } catch (error: SecurityException) {
            ListExpensesCallResult(
                message = "Caller lacks EXECUTE_APP_FUNCTIONS permission: ${error.message}",
                expenses = emptyList()
            )
        } catch (error: Throwable) {
            ListExpensesCallResult(
                message = "Call failed: ${error.message}",
                expenses = emptyList()
            )
        }
    }

    private fun parseAddExpenseSuccess(response: ExecuteAppFunctionResponse.Success): AddExpenseCallResult {
        val resultData = response.returnValue.getAppFunctionData(ExecuteAppFunctionResponse.Success.PROPERTY_RETURN_VALUE)
            ?: response.returnValue

        val balances = resultData.getAppFunctionDataList("balances")
            ?.map { item ->
                BalanceItem(
                    member = item.getString("member") ?: "",
                    balanceCents = item.getLong("balanceCents", 0L)
                )
            }
            .orEmpty()

        val expenseId = if (resultData.containsKey("expenseId")) {
            resultData.getLong("expenseId", 0L)
        } else {
            null
        }

        return AddExpenseCallResult(
            success = resultData.getBoolean("success", false),
            message = resultData.getString("message") ?: "No message",
            expenseId = expenseId,
            balances = balances
        )
    }

    private fun parseListExpensesSuccess(response: ExecuteAppFunctionResponse.Success): ListExpensesCallResult {
        val resultData = response.returnValue.getAppFunctionData(ExecuteAppFunctionResponse.Success.PROPERTY_RETURN_VALUE)
            ?: response.returnValue

        val expenses = resultData.getAppFunctionDataList("expenses")
            ?.map { item ->
                ExpenseItem(
                    id = item.getLong("id", 0L),
                    description = item.getString("description") ?: "",
                    amountCents = item.getLong("amountCents", 0L),
                    paidBy = item.getString("paidBy") ?: "",
                    participants = item.getStringList("participants") ?: emptyList()
                )
            }
            .orEmpty()

        return ListExpensesCallResult(
            message = "Loaded ${expenses.size} expense(s).",
            expenses = expenses
        )
    }

    private suspend fun findFunctionMetadata(
        manager: AppFunctionManager,
        functionId: String
    ): AppFunctionMetadata? {
        val searchSpec = AppFunctionSearchSpec(packageNames = setOf(targetPackage))
        return manager.observeAppFunctions(searchSpec)
            .first()
            .flatMap { it.appFunctions }
            .firstOrNull { it.id == functionId }
    }
}
