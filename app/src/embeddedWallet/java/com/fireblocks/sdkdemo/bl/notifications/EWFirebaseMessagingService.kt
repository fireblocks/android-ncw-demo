package com.fireblocks.sdkdemo.bl.notifications

import com.fireblocks.sdkdemo.FireblocksManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import timber.log.Timber

class EWFirebaseMessagingService : FirebaseMessagingService() {

    private val gson = Gson()

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.d("Message received:${message.messageId}")

        runCatching {
            val jsonData = gson.toJson(message.data)
            val notificationPayload = gson.fromJson(jsonData, NotificationPayload::class.java)

            if (notificationPayload.isEmpty()) {
                Timber.w("notification data is empty, ${message.messageId}")
            } else {
                Timber.d("Notification data: $notificationPayload")
                FireblocksManager.getInstance().handleNotificationPayload(applicationContext, notificationPayload)
            }
        }.onFailure { e ->
            Timber.e(e, "Error parsing notification data for message: ${message.messageId}")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.i("New token received: $token")
        FireblocksManager.getInstance().registerPushNotificationToken(applicationContext, token)
    }
}