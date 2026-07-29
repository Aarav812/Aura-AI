package com.aura.ai

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aura.ai.core.ui.components.EmptyState
import com.aura.ai.core.ui.components.GradientButton
import com.aura.ai.core.ui.theme.AuraTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ComposeComponentsTest {

    @get:Rule val composeRule = createComposeRule()

    @Test fun gradientButton_click_invokesCallback() {
        var clicked = false
        composeRule.setContent {
            AuraTheme { GradientButton("Send", onClick = { clicked = true }) }
        }
        composeRule.onNodeWithText("Send").performClick()
        assertTrue(clicked)
    }

    @Test fun emptyState_showsTitleAndSubtitle() {
        composeRule.setContent {
            AuraTheme {
                EmptyState(Icons.Rounded.AutoAwesome, "No chats", "Start a conversation")
            }
        }
        composeRule.onNodeWithText("No chats").assertIsDisplayed()
        composeRule.onNodeWithText("Start a conversation").assertIsDisplayed()
    }
}
