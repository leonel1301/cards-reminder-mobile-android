package com.lenaralabs.cardsreminder.core.network

import com.lenaralabs.cardsreminder.core.auth.AuthRepository
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class ApiService(
    private val authRepository: AuthRepository,
) {
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json".toMediaType()
    @PublishedApi
    internal val json = Json { ignoreUnknownKeys = true }

    suspend inline fun <reified T> getDecoded(
        path: String,
        authenticated: Boolean = true,
    ): T = json.decodeFromString(get(path, authenticated))

    suspend inline fun <reified T, reified B> postDecoded(
        path: String,
        body: B,
        authenticated: Boolean = true,
    ): T = json.decodeFromString(post(path, json.encodeToString(body), authenticated))

    suspend inline fun <reified T> patchDecoded(
        path: String,
        authenticated: Boolean = true,
    ): T = json.decodeFromString(patch(path, body = null, authenticated))

    suspend inline fun <reified T, reified B> patchDecoded(
        path: String,
        body: B,
        authenticated: Boolean = true,
    ): T = json.decodeFromString(patch(path, json.encodeToString(body), authenticated))

    suspend inline fun <reified T, reified B> putDecoded(
        path: String,
        body: B,
        authenticated: Boolean = true,
    ): T = json.decodeFromString(put(path, json.encodeToString(body), authenticated))

    suspend fun get(
        path: String,
        authenticated: Boolean = true,
    ): String = performRequest(path = path, method = "GET", body = null, authenticated = authenticated)

    suspend fun post(
        path: String,
        body: String,
        authenticated: Boolean = true,
    ): String = performRequest(path = path, method = "POST", body = body, authenticated = authenticated)

    suspend fun patch(
        path: String,
        body: String? = null,
        authenticated: Boolean = true,
    ): String = performRequest(path = path, method = "PATCH", body = body, authenticated = authenticated)

    suspend fun put(
        path: String,
        body: String,
        authenticated: Boolean = true,
    ): String = performRequest(path = path, method = "PUT", body = body, authenticated = authenticated)

    suspend fun delete(
        path: String,
        authenticated: Boolean = true,
    ): String = performRequest(path = path, method = "DELETE", body = null, authenticated = authenticated)

    suspend fun delete(
        path: String,
        body: String,
        authenticated: Boolean = true,
    ): String = performRequest(path = path, method = "DELETE", body = body, authenticated = authenticated)

    suspend inline fun <reified B> delete(
        path: String,
        body: B,
        authenticated: Boolean = true,
    ): String = delete(path, json.encodeToString(body), authenticated)

    private suspend fun performRequest(
        path: String,
        method: String,
        body: String?,
        authenticated: Boolean,
    ): String = withContext(Dispatchers.IO) {
        val requestBuilder = Request.Builder()
            .url(buildUrl(path))
            .header("Accept", "application/json")
            .header("Accept-Language", acceptLanguageHeaderValue())

        when (method) {
            "GET" -> requestBuilder.get()
            "DELETE" -> {
                if (body != null) {
                    requestBuilder.delete(body.toRequestBody(jsonMediaType))
                    requestBuilder.header("Content-Type", "application/json")
                } else {
                    requestBuilder.delete()
                }
            }
            "POST", "PUT", "PATCH" -> {
                val requestBody = body?.toRequestBody(jsonMediaType)
                    ?: ByteArray(0).toRequestBody(null, 0, 0)
                when (method) {
                    "POST" -> requestBuilder.post(requestBody)
                    "PUT" -> requestBuilder.put(requestBody)
                    else -> requestBuilder.patch(requestBody)
                }
                if (body != null) {
                    requestBuilder.header("Content-Type", "application/json")
                }
            }
            else -> requestBuilder.method(method, body?.toRequestBody(jsonMediaType))
        }

        if (authenticated) {
            val token = authRepository.getIdToken()
                ?: throw ApiException.NotAuthenticated()
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val response = client.newCall(requestBuilder.build()).execute()
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            throw ApiException.ServerError(
                statusCode = response.code,
                message = parseServerErrorMessage(responseBody),
            )
        }

        responseBody
    }

    private fun buildUrl(path: String): String {
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        return ApiConfig.BASE_URL.trimEnd('/') + normalizedPath
    }

    private fun acceptLanguageHeaderValue(): String {
        return Locale.getDefault().language.ifBlank { "es" }
    }

    private fun parseServerErrorMessage(body: String): String? {
        if (body.isBlank()) return null

        return runCatching {
            val json = JSONObject(body)
            sequenceOf("error", "message")
                .mapNotNull { key ->
                    json.optString(key).takeIf { it.isNotBlank() }
                }
                .firstOrNull()
        }.getOrNull() ?: body.takeIf { !it.startsWith("{") && !it.startsWith("[") }
    }
}
