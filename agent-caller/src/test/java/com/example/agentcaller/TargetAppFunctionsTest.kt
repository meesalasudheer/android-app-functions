package com.example.agentcaller

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TargetAppFunctionsTest {
    @Test
    fun targetPackage_andFunctionIds_useExpectedNamespace() {
        assertThat(TargetAppFunctions.TARGET_PACKAGE).isEqualTo("com.example.appfunctionsdemo")
        assertThat(TargetAppFunctions.ADD_SHARED_EXPENSE_ID)
            .isEqualTo("com.example.appfunctionsdemo.functions.ExpenseAgentFunctions#addSharedExpense")
        assertThat(TargetAppFunctions.LIST_RECENT_EXPENSES_ID)
            .isEqualTo("com.example.appfunctionsdemo.functions.ExpenseAgentFunctions#listRecentExpenses")
    }
}
