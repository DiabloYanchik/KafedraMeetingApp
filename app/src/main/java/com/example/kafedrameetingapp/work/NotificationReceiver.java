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
    @Override
    public void onReceive(Context context, Intent intent) {
        String channelId = "meeting_channel";
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Meeting Notifications", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        int protocolNumber = intent.getIntExtra("protocolNumber", 0);
        String topic = intent.getStringExtra("topic");
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");
        String roomNumber = intent.getStringExtra("roomNumber");

        String message = "Заседание скоро начнется.\n" +
                "Тема: " + (topic != null ? topic : "Не указано") + "\n" +
                "Дата: " + (date != null ? date : "") + " " + (time != null ? time : "") +
                (protocolNumber != 0 ? "\nПротокол №" + protocolNumber : "") +
                (roomNumber != null ? "\nКабинет: " + roomNumber : "");

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Скоро заседание кафедры!")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}