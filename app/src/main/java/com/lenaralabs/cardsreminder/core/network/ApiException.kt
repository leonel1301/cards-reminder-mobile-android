package com.lenaralabs.cardsreminder.core.network

sealed class ApiException(message: String) : Exception(message) {
    class NotAuthenticated : ApiException("No active session.")
    class InvalidResponse : ApiException("Invalid server response.")
    class DecodingFailed(detail: String) : ApiException(detail)
    class ServerError(val statusCode: Int, message: String?) : ApiException(
        message ?: "Server error ($statusCode).",
    )
}
