package com.example.rmcfrontend.compose.screens.terms

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rmcfrontend.api.TermsApi
import com.example.rmcfrontend.api.models.CreateTermRequest
import com.example.rmcfrontend.api.models.UpdateTermRequest
import com.example.rmcfrontend.api.models.response.GetTermResponse
import com.example.rmcfrontend.compose.theme.RmcTheme
import com.example.rmcfrontend.compose.viewmodel.TermsViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TermsScreenUiTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private class FakeTermsApi(initial: List<GetTermResponse> = emptyList()) : TermsApi {
        private val terms = initial.toMutableList()
        private var nextId = (terms.maxOfOrNull { it.id ?: 0L } ?: 0L) + 1L
        private var nextVersion = (terms.maxOfOrNull { it.version ?: 0 } ?: 0) + 1

        override suspend fun getActiveTermForUser(userId: Long): GetTermResponse {
            // For test purposes, return the active term if present; otherwise a placeholder.
            return terms.firstOrNull { it.active == true }
                ?: terms.firstOrNull()
                ?: GetTermResponse(id = null, title = null, content = null, version = null, active = null)
        }

        override suspend fun getMyTerms(): List<GetTermResponse> {
            // Return a copy so callers cannot mutate.
            return terms.toList()
        }

        override suspend fun getTermById(id: Long): GetTermResponse {
            return terms.firstOrNull { it.id == id }
                ?: GetTermResponse(id = id, title = null, content = null, version = null, active = null)
        }

        override suspend fun createTerm(request: CreateTermRequest): GetTermResponse {
            val created = GetTermResponse(
                id = nextId++,
                title = request.title,
                content = request.content,
                version = nextVersion++,
                active = request.active,
                createdAt = "2026-01-18",
                modifiedAt = "2026-01-18"
            )
            terms.add(created)
            return created
        }

        override suspend fun updateTerm(body: UpdateTermRequest) {
            val idx = terms.indexOfFirst { it.id == body.id }
            val updated = GetTermResponse(
                id = body.id,
                title = body.title,
                content = body.content,
                version = (terms.getOrNull(idx)?.version ?: 0) + 1,
                active = body.active,
                createdAt = terms.getOrNull(idx)?.createdAt,
                modifiedAt = "2026-01-18"
            )
            if (idx >= 0) terms[idx] = updated else terms.add(updated)
        }

        override suspend fun deleteTerm(id: Long) {
            terms.removeAll { it.id == id }
        }
    }

    @Test
    fun emptyTerms_createFlow_addsItemAndShowsInList() {
        val fakeApi = FakeTermsApi()
        val vm = TermsViewModel(fakeApi)

        rule.setContent {
            RmcTheme {
                TermsScreen(vm = vm)
            }
        }

        // Empty state -> create
        rule.onNodeWithTag("terms_empty_create").assertIsDisplayed().performClick()

        rule.onNodeWithTag("terms_title_field").performTextInput("Version 1")
        rule.onNodeWithTag("terms_content_field").performTextInput("Hello terms")
        rule.onNodeWithTag("terms_save_button").assertIsDisplayed().performClick()

        // Item should appear as a card title.
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodes(hasText("Version 1")).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Version 1").assertIsDisplayed()
    }

    @Test
    fun addButton_opensDialog_whenListNotEmpty() {
        val fakeApi = FakeTermsApi(
            listOf(GetTermResponse(id = 1L, title = "Existing", content = "c", version = 1, active = true))
        )
        val vm = TermsViewModel(fakeApi)

        rule.setContent {
            RmcTheme {
                TermsScreen(vm = vm)
            }
        }

        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodes(hasText("Existing")).fetchSemanticsNodes().isNotEmpty()
        }

        rule.onNodeWithTag("terms_add").assertIsDisplayed().performClick()
        rule.onNodeWithTag("terms_title_field").assertIsDisplayed()
    }
}
