package com.lenaralabs.cardsreminder.core.model

import androidx.compose.ui.graphics.Color
import com.lenaralabs.cardsreminder.core.util.toComposeColor
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiCard(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("owner_id") val ownerId: String,
    val name: String,
    @SerialName("last_four_digits") val lastFourDigits: String,
    val issuer: String? = null,
    @SerialName("billing_cycle_day") val billingCycleDay: Int,
    @SerialName("payment_due_day") val paymentDueDay: Int,
    @SerialName("color_hex") val colorHex: String? = null,
    val notes: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
) {
    val periodEndDay: Int get() = billingCycleDay

    val periodStartDay: Int
        get() = if (billingCycleDay >= 31) 1 else billingCycleDay + 1

    val paymentDay: Int get() = paymentDueDay

    val displayColorHex: String
        get() = colorHex
            ?.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
            ?.takeIf { it.isNotEmpty() }
            ?: "808080"

    val color: Color get() = displayColorHex.toComposeColor()

    val maskedNumber: String
        get() = if (lastFourDigits == "0000") "•••• •••• •••• ••••"
        else "•••• •••• •••• $lastFourDigits"
}
