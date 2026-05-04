package com.example.appfunctionsdemo

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import org.junit.Test

class AppFunctionsDemoScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun addExpense_fromUi_showsSuccessMessage() {
        composeRule.onNodeWithTag("descriptionField").performTextInput("Movie")
        composeRule.onNodeWithTag("amountField").performTextClearance()
        composeRule.onNodeWithTag("amountField").performTextInput("1200")
        composeRule.onNodeWithTag("payerField").performTextClearance()
        composeRule.onNodeWithTag("payerField").performTextInput("Alex")
        composeRule.onNodeWithTag("participantsField").performTextClearance()
        composeRule.onNodeWithTag("participantsField").performTextInput("Alex,Sam")

        composeRule.onNodeWithTag("saveButton").performClick()

        composeRule.onNodeWithTag("statusMessage").assertIsDisplayed()
    }
}
