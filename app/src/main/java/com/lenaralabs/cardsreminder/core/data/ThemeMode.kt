package com.lenaralabs.cardsreminder.core.data

enum class ThemeMode(val storageValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark"),
    ;

    companion object {
        fun fromStorage(value: String?): ThemeMode =
            entries.find { it.storageValue == value } ?: SYSTEM
    }
}
