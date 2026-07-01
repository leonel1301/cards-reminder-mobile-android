package com.lenaralabs.cardsreminder.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiDevice(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("fcm_token") val fcmToken: String,
    val platform: String,
    val language: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class RegisterDeviceRequest(
    @SerialName("fcm_token") val fcmToken: String,
    val platform: String,
    val language: String,
    val timezone: String,
)

@Serializable
data class UnregisterDeviceRequest(
    @SerialName("fcm_token") val fcmToken: String,
)
