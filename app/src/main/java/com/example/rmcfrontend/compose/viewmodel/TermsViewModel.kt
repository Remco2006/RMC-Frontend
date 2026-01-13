package com.example.rmcfrontend.compose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rmcfrontend.api.ApiClient
import com.example.rmcfrontend.api.TermsApi
import com.example.rmcfrontend.api.models.CreateTermRequest
import com.example.rmcfrontend.api.models.UpdateTermRequest
import com.example.rmcfrontend.api.models.response.GetTermResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class TermsItem(
    val id: Long,
    val title: String,
    val content: String,
    val version: Int,
    val isActive: Boolean,
    val createdAt: String?,
    val modifiedAt: String?
)

data class TermsState(
    val isBusy: Boolean = false,
    val errorMessage: String? = null,
    val items: List<TermsItem> = emptyList()
)

/**
 * Terms & Conditions CRUD backed by AvansAPI /terms endpoints.
 *
 * Single-active rule is enforced by the backend (SqlTermRepository), but we also
 * refresh after each mutation to keep UI consistent.
 */
class TermsViewModel(
    private val termsApi: TermsApi = ApiClient.termsApi
) : ViewModel() {

    private val _state = MutableStateFlow(TermsState())
    val state: StateFlow<TermsState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            try {
                val items = termsApi.getMyTerms()
                    .map { it.toUi() }
                    .sortedByDescending { it.version }
                _state.value = TermsState(isBusy = false, items = items)
            } catch (e: HttpException) {
                val msg = safeHttpMessage(e)
                _state.value = _state.value.copy(isBusy = false, errorMessage = msg)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isBusy = false, errorMessage = "Could not load terms: ${e.message}")
            }
        }
    }

    fun create(title: String, content: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            try {
                val existing = _state.value.items
                // Auto-active if none active yet (same UX as before)
                val makeActive = existing.none { it.isActive }
                termsApi.createTerm(
                    CreateTermRequest(
                        title = title.trim(),
                        content = content.trim(),
                        active = makeActive
                    )
                )
                load()
            } catch (e: HttpException) {
                _state.value = _state.value.copy(isBusy = false, errorMessage = safeHttpMessage(e))
            } catch (e: Exception) {
                _state.value = _state.value.copy(isBusy = false, errorMessage = "Create failed: ${e.message}")
            }
        }
    }

    fun update(id: Long, title: String, content: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            try {
                val existing = _state.value.items.firstOrNull { it.id == id }
                    ?: throw IllegalArgumentException("Term not found")

                termsApi.updateTerm(
                    UpdateTermRequest(
                        id = id,
                        title = title.trim(),
                        content = content.trim(),
                        active = existing.isActive
                    )
                )
                load()
            } catch (e: HttpException) {
                _state.value = _state.value.copy(isBusy = false, errorMessage = safeHttpMessage(e))
            } catch (e: Exception) {
                _state.value = _state.value.copy(isBusy = false, errorMessage = "Update failed: ${e.message}")
            }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            try {
                termsApi.deleteTerm(id)
                load()
            } catch (e: HttpException) {
                _state.value = _state.value.copy(isBusy = false, errorMessage = safeHttpMessage(e))
            } catch (e: Exception) {
                _state.value = _state.value.copy(isBusy = false, errorMessage = "Delete failed: ${e.message}")
            }
        }
    }

    /**
     * Set one term ACTIVE. Backend automatically deactivates others for the same user.
     */
    fun setActive(id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isBusy = true, errorMessage = null)
            try {
                val target = _state.value.items.firstOrNull { it.id == id }
                    ?: throw IllegalArgumentException("Term not found")

                // Only send the activate request; backend enforces single-active.
                termsApi.updateTerm(
                    UpdateTermRequest(
                        id = target.id,
                        title = target.title,
                        content = target.content,
                        active = true
                    )
                )
                load()
            } catch (e: HttpException) {
                _state.value = _state.value.copy(isBusy = false, errorMessage = safeHttpMessage(e))
            } catch (e: Exception) {
                _state.value = _state.value.copy(isBusy = false, errorMessage = "Activate failed: ${e.message}")
            }
        }
    }

    private fun GetTermResponse.toUi(): TermsItem {
        return TermsItem(
            id = id ?: 0L,
            title = title.orEmpty(),
            content = content.orEmpty(),
            version = version ?: 0,
            isActive = active == true,
            createdAt = createdAt,
            modifiedAt = modifiedAt
        )
    }

    private fun safeHttpMessage(e: HttpException): String {
        val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
        return when (e.code()) {
            401 -> "Unauthorized (401). Please login again."
            403 -> "Forbidden (403)."
            404 -> "Not found (404)."
            else -> body?.takeIf { it.isNotBlank() } ?: "HTTP ${e.code()} error"
        }
    }
}
