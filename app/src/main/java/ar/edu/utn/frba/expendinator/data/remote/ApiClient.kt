package ar.edu.utn.frba.expendinator.data.remote

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
