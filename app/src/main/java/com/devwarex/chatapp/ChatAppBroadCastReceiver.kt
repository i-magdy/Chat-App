package com.devwarex.chatapp

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.devwarex.chatapp.ui.chat.ChatsActivity
import com.devwarex.chatapp.ui.conversation.ConversationActivity
import com.devwarex.chatapp.util.BroadCastUtility


class ChatAppBroadCastReceiver : BroadcastReceiver() {

    var currentChatId = ""

    @SuppressLint("UnsafeProtectedBroadcastReceiver", "ServiceCast")
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            if (intent.hasExtra(BroadCastUtility.CHAT_ID)) {
                currentChatId = intent.getStringExtra(BroadCastUtility.CHAT_ID) ?: ""
            }

            val payload: Map<String, String?> = mapOf(
                "title" to intent.getStringExtra("payload_title"),
                "body" to intent.getStringExtra("payload_body"),
                "id" to intent.getStringExtra("payload_id")
            )
            notify(context = context!!, payload = payload, currentChatId)
        }
    }

    private fun notify(
        context: Context,
        payload: Map<String, String?>,
        currentChatId: String
    ) {
        Log.e("TAG_BROAD_CHAT", "notify broad cast")
        val title: String = payload["title"] ?: ""
        val body: String = payload["body"] ?: "test!"
        val id: String = payload["id"] ?: ""
        if (title.isEmpty()) return
        var defaultIntent = Intent(context, ConversationActivity::class.java)
        defaultIntent.action = AccessibilityService.SERVICE_INTERFACE
        if (id.isNotEmpty() && id != currentChatId) {
            defaultIntent.putExtra("chat_id", id)
        } else {
            defaultIntent = Intent(context, ChatsActivity::class.java)
        }
        Log.e("create", "flag + ${Build.VERSION.SDK_INT}")
        val defaultPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                context, 0,
                defaultIntent, PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getActivity(
                context, 0,
                defaultIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        Log.e("DONE", "flag + ${Build.VERSION.SDK_INT}")

        var bitmap: Bitmap? = null
        if (context.resources != null) {
            bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.user)
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val CHANNEL_ID = "chat:$id"
        val name = "channel name"
        val descriptionText = "channel description"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            enableLights(true)
        }
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_message)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(defaultPendingIntent)
            .setAutoCancel(true)
        if (bitmap != null) {
            builder.setLargeIcon(bitmap)
        }

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(7001, builder.build())
        }
    }
}