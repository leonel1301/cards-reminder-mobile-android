package com.lenaralabs.cardsreminder.core.data

import com.lenaralabs.cardsreminder.core.model.ApiDevice
import com.lenaralabs.cardsreminder.core.model.RegisterDeviceRequest
import com.lenaralabs.cardsreminder.core.model.UnregisterDeviceRequest
import com.lenaralabs.cardsreminder.core.network.ApiService

class DevicesRepository(
    private val apiService: ApiService,
) {
    suspend fun register(
        fcmToken: String,
        language: String,
        timezone: String,
    ): ApiDevice {
        return apiService.putDecoded(
            path = "/devices",
            body = RegisterDeviceRequest(
                fcmToken = fcmToken,
                platform = PLATFORM,
                language = language,
                timezone = timezone,
            ),
        )
    }

    suspend fun unregister(fcmToken: String) {
        apiService.delete(
            path = "/devices",
            body = UnregisterDeviceRequest(fcmToken = fcmToken),
        )
    }

    private companion object {
        const val PLATFORM = "android"
    }
}
