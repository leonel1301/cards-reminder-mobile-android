package com.lenaralabs.cardsreminder.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiUser(
    val id: String,
    @SerialName("firebase_uid") val firebaseUid: String,
    val email: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("terms_accepted_at") val termsAcceptedAt: String? = null,
    @SerialName("privacy_accepted_at") val privacyAcceptedAt: String? = null,
    @SerialName("terms_version") val termsVersion: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)
