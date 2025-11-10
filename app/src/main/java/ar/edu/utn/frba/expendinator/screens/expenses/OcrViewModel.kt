package ar.edu.utn.frba.expendinator.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.utn.frba.expendinator.data.remote.ApiClient
import ar.edu.utn.frba.expendinator.model.dto.*
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OcrUiState {
    object Idle : OcrUiState()
    object Loading : OcrUiState()
    data class Preview(val data: OcrPreview, val editable: MutableList<OcrItem>) : OcrUiState()
    object Confirming : OcrUiState()
    object Confirmed : OcrUiState()
    data class Error(val message: String) : OcrUiState()
}

class OcrViewModel : ViewModel() {
    private val _ui = MutableStateFlow<OcrUiState>(OcrUiState.Idle)
    val ui = _ui.asStateFlow()

    private val client = ApiClient.client
    private val baseUrl = ApiClient.baseUrl

    fun loadMock() {
        viewModelScope.launch {
            _ui.value = OcrUiState.Loading
            try {
                val resp = client.get("$baseUrl/ocr/mock")
                if (!resp.status.isSuccess()) {
                    _ui.value = OcrUiState.Error("Mock OCR failed (${resp.status.value})")
                    return@launch
                }
                val preview = resp.body<OcrPreview>()
                _ui.value = OcrUiState.Preview(
                    data = preview,
                    editable = preview.items.map { it.copy() }.toMutableList()
                )
            } catch (e: Exception) {
                _ui.value = OcrUiState.Error(e.message ?: "Network error")
            }
        }
    }

    fun confirm(onDone: () -> Unit) {
        val current = _ui.value
        if (current !is OcrUiState.Preview) return

        viewModelScope.launch {
            _ui.value = OcrUiState.Confirming
            try {
                val resp = client.post("$baseUrl/ocr/confirm") {
                    contentType(ContentType.Application.Json)
                    setBody(OcrConfirmRequest(items = current.editable))
                }
                if (!resp.status.isSuccess()) {
                    _ui.value = OcrUiState.Error("Confirm failed (${resp.status.value})")
                    return@launch
                }
                _ui.value = OcrUiState.Confirmed
                onDone()
            } catch (e: Exception) {
                _ui.value = OcrUiState.Error(e.message ?: "Network error")
            }
        }
    }
}
