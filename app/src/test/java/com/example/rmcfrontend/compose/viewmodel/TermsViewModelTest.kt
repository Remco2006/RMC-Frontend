package com.example.rmcfrontend.compose.viewmodel

import com.example.rmcfrontend.api.TermsApi
import com.example.rmcfrontend.api.models.CreateTermRequest
import com.example.rmcfrontend.api.models.response.GetTermResponse
import com.example.rmcfrontend.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TermsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun load_sortsByVersionDescending() = runTest(mainDispatcherRule.dispatcher) {
        val termsApi = mockk<TermsApi>()
        coEvery { termsApi.getMyTerms() } returns listOf(
            GetTermResponse(id = 1L, title = "v1", content = "c", version = 1, active = false),
            GetTermResponse(id = 2L, title = "v3", content = "c", version = 3, active = true),
            GetTermResponse(id = 3L, title = "v2", content = "c", version = 2, active = false)
        )

        val vm = TermsViewModel(termsApi)
        vm.load()
        advanceUntilIdle()

        val items = vm.state.value.items
        assertEquals(listOf("v3", "v2", "v1"), items.map { it.title })
    }

    @Test
    fun create_whenNoActive_setsActiveTrueInRequest() = runTest(mainDispatcherRule.dispatcher) {
        val termsApi = mockk<TermsApi>()

        // First load: no terms. After create: returns the created one.
        coEvery { termsApi.getMyTerms() } returnsMany listOf(
            emptyList(),
            listOf(GetTermResponse(id = 10L, title = "T", content = "C", version = 1, active = true))
        )
        coEvery { termsApi.createTerm(any()) } returns GetTermResponse(id = 10L, title = "T", content = "C", version = 1, active = true)

        val vm = TermsViewModel(termsApi)
        vm.load()
        advanceUntilIdle()

        vm.create("T", "C")
        advanceUntilIdle()

        coVerify {
            termsApi.createTerm(match { it.title == "T" && it.content == "C" && it.active })
        }
        coVerify(exactly = 2) { termsApi.getMyTerms() }
    }

    @Test
    fun create_whenActiveExists_setsActiveFalseInRequest() = runTest(mainDispatcherRule.dispatcher) {
        val termsApi = mockk<TermsApi>()

        coEvery { termsApi.getMyTerms() } returnsMany listOf(
            listOf(GetTermResponse(id = 1L, title = "Existing", content = "c", version = 1, active = true)),
            listOf(
                GetTermResponse(id = 1L, title = "Existing", content = "c", version = 1, active = true),
                GetTermResponse(id = 2L, title = "New", content = "n", version = 2, active = false)
            )
        )
        coEvery { termsApi.createTerm(any()) } returns GetTermResponse(id = 2L, title = "New", content = "n", version = 2, active = false)

        val vm = TermsViewModel(termsApi)
        vm.load()
        advanceUntilIdle()

        vm.create("New", "n")
        advanceUntilIdle()

        coVerify {
            termsApi.createTerm(match { it.title == "New" && it.content == "n" && !it.active })
        }
    }
}
