package com.lenaralabs.cardsreminder.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiTerms(
    @SerialName("terms_version") val termsVersion: String,
)

fun ApiUser.hasAcceptedTerms(requiredVersion: String): Boolean {
    if (termsAcceptedAt.isNullOrBlank() || privacyAcceptedAt.isNullOrBlank()) return false
    if (termsVersion != requiredVersion) return false
    return true
}
