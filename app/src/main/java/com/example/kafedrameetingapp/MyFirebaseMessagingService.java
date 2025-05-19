package com.example.kafedrameetingapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.kafedrameetingapp.work.NotificationReceiver;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessagingService";
    private static final String CHANNEL_ID = "meeting_channel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "FCM Message received: " + remoteMessage.getData());
        Log.d(TAG, "Notification: " + remoteMessage.getNotification());

        String title = null;
        String message = null;

        // Проверяем data payload (используется в нашем коде)
        if (remoteMessage.getData().size() > 0) {
            title = remoteMessage.getData().get("title");
            message = remoteMessage.getData().get("message");
        }

        // Проверяем notification payload (для сообщений из Firebase Console)
        if (remoteMessage.getNotification() != null) {
            title = title != null ? title : remoteMessage.getNotification().getTitle();
            message = message != null ? message : remoteMessage.getNotification().getBody();
        }

        if (title != null && message != null) {
            Log.d(TAG, "Processing FCM - Title: " + title + ", Message: " + message);

            // Отправляем данные в NotificationReceiver
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("title", title);
            intent.putExtra("message", message);
            sendBroadcast(intent);
            Log.d(TAG, "Broadcast sent to NotificationReceiver: " + title);

            // Показываем уведомление напрямую
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID, "Meeting Notifications", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Notifications for upcoming department meetings");
                manager.createNotificationChannel(channel);
            }

            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            manager.notify((int) System.currentTimeMillis(), builder.build());
            Log.d(TAG, "Notification displayed: " + title);
        } else {
            Log.w(TAG, "Empty title or message in FCM data/notification");
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM Token: " + token);
    }
}