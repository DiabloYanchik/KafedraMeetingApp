package com.example.kafedrameetingapp.work;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.kafedrameetingapp.MainActivity;
import com.example.kafedrameetingapp.R;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "meeting_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Создаем канал уведомлений
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Meeting Notifications", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for upcoming department meetings");
            manager.createNotificationChannel(channel);
        }

        // Извлекаем данные из Intent (для локальных уведомлений, если они остались)
        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        // Если данные пришли через FCM
        if (title == null || message == null) {
            title = intent.getStringExtra("gcm.notification.title");
            message = intent.getStringExtra("gcm.notification.body");
            if (title == null) {
                title = "Скоро заседание кафедры!";
            }
            if (message == null) {
                message = "Заседание скоро начнется.";
            }
        }

        // Создаем PendingIntent для перехода в MainActivity
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Строим уведомление
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Отображаем уведомление
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}