package com.lenaralabs.cardsreminder.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCardRequest(
    val name: String,
    @SerialName("last_four_digits") val lastFourDigits: String,
    val issuer: String? = null,
    @SerialName("billing_cycle_day") val billingCycleDay: Int,
    @SerialName("payment_due_day") val paymentDueDay: Int,
    @SerialName("color_hex") val colorHex: String? = null,
    val notes: String? = null,
    @SerialName("owner_id") val ownerId: String? = null,
)

@Serializable
data class UpdateCardRequest(
    val name: String? = null,
    @SerialName("last_four_digits") val lastFourDigits: String? = null,
    val issuer: String? = null,
    @SerialName("billing_cycle_day") val billingCycleDay: Int? = null,
    @SerialName("payment_due_day") val paymentDueDay: Int? = null,
    @SerialName("color_hex") val colorHex: String? = null,
    val notes: String? = null,
    @SerialName("is_active") val isActive: Boolean? = null,
    @SerialName("owner_id") val ownerId: String? = null,
)
