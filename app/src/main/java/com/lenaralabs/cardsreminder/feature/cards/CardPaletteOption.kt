package com.lenaralabs.cardsreminder.feature.cards

import androidx.annotation.StringRes
import com.lenaralabs.cardsreminder.R

data class CardPaletteOption(
    val hex: String,
    @param:StringRes val nameRes: Int,
) {
    companion object {
        const val DEFAULT_HEX = "3B82F6"

        val all = listOf(
            CardPaletteOption("3B82F6", R.string.color_blue),
            CardPaletteOption("6366F1", R.string.color_indigo),
            CardPaletteOption("8B5CF6", R.string.color_violet),
            CardPaletteOption("22C55E", R.string.color_green),
            CardPaletteOption("10B981", R.string.color_emerald),
            CardPaletteOption("F97316", R.string.color_orange),
            CardPaletteOption("EF4444", R.string.color_red),
            CardPaletteOption("EC4899", R.string.color_pink),
            CardPaletteOption("EAB308", R.string.color_yellow),
            CardPaletteOption("14B8A6", R.string.color_teal),
            CardPaletteOption("171717", R.string.color_black),
            CardPaletteOption("6B7280", R.string.color_gray),
        )

        fun normalize(hex: String?): String {
            return hex
                ?.filter { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' }
                ?.uppercase()
                .orEmpty()
        }

        fun matching(hex: String?): String {
            val normalized = normalize(hex)
            return all.firstOrNull { it.hex == normalized }?.hex ?: DEFAULT_HEX
        }
    }
}
