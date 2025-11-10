package ar.edu.utn.frba.expendinator.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ar.edu.utn.frba.expendinator.data.remote.ApiClient
import ar.edu.utn.frba.expendinator.model.dto.LoginRequest
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException


sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

@Serializable
data class LoginResponse(val token: String, val user: UserDto)

@Serializable
data class UserDto(val id: Int, val email: String)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val client = ApiClient.client
    private val baseUrl = ApiClient.baseUrl

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = client.post("$baseUrl/auth/login") {
                    contentType(ContentType.Application.Json)
                    headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    setBody(LoginRequest(email = email, password = password))
                }

                if (response.status.isSuccess()) {
                    // Parseamos la respuesta y guardamos el token globalmente
                    val body = response.body<LoginResponse>()
                    ApiClient.authToken = body.token
                    _uiState.value = AuthUiState.Success
                } else {
                    _uiState.value = AuthUiState.Error("Login failed (${response.status.value})")
                }
            } catch (e: UnknownHostException) {
                _uiState.value = AuthUiState.Error("No se puede resolver el host. ¿Base URL correcta?")
            } catch (e: ConnectTimeoutException) {
                _uiState.value = AuthUiState.Error("Timeout conectando al servidor")
            } catch (e: HttpRequestTimeoutException) {
                _uiState.value = AuthUiState.Error("Timeout de request")
            } catch (e: SocketTimeoutException) {
                _uiState.value = AuthUiState.Error("Timeout de socket")
            } catch (e: ConnectException) {
                _uiState.value = AuthUiState.Error("No se pudo conectar con el servidor")
            } catch (e: ClientRequestException) {
                _uiState.value = AuthUiState.Error("Error del cliente: ${e.response.status}")
            } catch (e: ServerResponseException) {
                _uiState.value = AuthUiState.Error("Error del servidor: ${e.response.status}")
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val response = client.post("$baseUrl/auth/register") {
                    contentType(ContentType.Application.Json)
                    headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
                    setBody(LoginRequest(email = email, password = password))
                }

                if (response.status.isSuccess()) {
                    // Podrías loguear automáticamente tras registrar: opcional
                    _uiState.value = AuthUiState.Success
                } else {
                    _uiState.value = AuthUiState.Error("Registration failed (${response.status.value})")
                }
            } catch (e: UnknownHostException) {
                _uiState.value = AuthUiState.Error("No se puede resolver el host. ¿Base URL correcta?")
            } catch (e: ConnectTimeoutException) {
                _uiState.value = AuthUiState.Error("Timeout conectando al servidor")
            } catch (e: HttpRequestTimeoutException) {
                _uiState.value = AuthUiState.Error("Timeout de request")
            } catch (e: SocketTimeoutException) {
                _uiState.value = AuthUiState.Error("Timeout de socket")
            } catch (e: ConnectException) {
                _uiState.value = AuthUiState.Error("No se pudo conectar con el servidor")
            } catch (e: ClientRequestException) {
                _uiState.value = AuthUiState.Error("Error del cliente: ${e.response.status}")
            } catch (e: ServerResponseException) {
                _uiState.value = AuthUiState.Error("Error del servidor: ${e.response.status}")
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}
