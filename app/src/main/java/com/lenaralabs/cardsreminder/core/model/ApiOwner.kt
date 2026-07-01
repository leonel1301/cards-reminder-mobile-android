package com.lenaralabs.cardsreminder.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiOwner(
    val id: String,
    @SerialName("user_id") val userId: String,
    val name: String,
    @SerialName("salary_day") val salaryDay: Int? = null,
    @SerialName("is_self") val isSelf: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)
