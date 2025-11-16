package ar.edu.utn.frba.expendinator.data.remote

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.request.headers
import kotlinx.serialization.json.Json

object ApiClient {

    var baseUrl: String = "http://10.0.2.2:3000"

    @Volatile
    var authToken: String? = null
        set(value) {
            field = value
            // Guardar en SharedPreferences cada vez que cambie
            appContext?.let { context ->
                val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("auth_token", value).apply()
            }
        }

    private var appContext: Context? = null

    /**
     * Debe ser llamado desde Application.onCreate()
     */
    fun init(context: Context) {
        appContext = context.applicationContext
        // Cargar el token guardado
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        authToken = prefs.getString("auth_token", null)
    }

    /**
     * Cerrar sesión (limpiar token)
     */
    fun logout() {
        authToken = null
    }

    /**
     * Verificar si hay sesión activa
     */
    fun isLoggedIn(): Boolean = !authToken.isNullOrEmpty()

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                }
            )
        }
        install(DefaultRequest) {
            headers {
                ApiClient.authToken?.let { token ->
                    set("Authorization", "Bearer $token")
                }
            }
        }
    }
}