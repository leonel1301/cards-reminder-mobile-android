package com.lenaralabs.cardsreminder.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.lenaralabs.cardsreminder.R

object NotificationChannels {
    const val PAYMENT_REMINDERS_ID = "payment_reminders"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            PAYMENT_REMINDERS_ID,
            context.getString(R.string.notification_channel_payment_reminders),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.notification_channel_payment_reminders_description)
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }
}
