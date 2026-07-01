package com.lenaralabs.cardsreminder.core.util

import android.os.Build

object DeviceInfo {
    fun feedbackDeviceString(): String {
        val model = Build.MODEL.orEmpty().ifBlank { "Android" }
        val release = Build.VERSION.RELEASE.orEmpty().ifBlank { "?" }
        return "$model · Android $release"
    }
}
