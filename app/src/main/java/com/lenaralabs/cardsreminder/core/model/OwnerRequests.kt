package com.lenaralabs.cardsreminder.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateOwnerRequest(
    val name: String,
    @SerialName("salary_day") val salaryDay: Int? = null,
)

@Serializable
data class UpdateOwnerRequest(
    val name: String? = null,
    @SerialName("salary_day") val salaryDay: Int? = null,
)
