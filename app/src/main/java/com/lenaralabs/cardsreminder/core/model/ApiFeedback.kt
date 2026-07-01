package com.lenaralabs.cardsreminder.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiFeedback(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val device: String,
    val content: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class CreateFeedbackRequest(
    val title: String,
    val device: String,
    val content: String,
)

@Serializable
data class UpdateFeedbackRequest(
    val title: String? = null,
    val device: String? = null,
    val content: String? = null,
)
