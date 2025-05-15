package com.example.kafedrameetingapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class AlarmUtils {
    public static void scheduleAlarms(Context context, Calendar meetingTime, int protocolNumber) {
        // За 2 часа
        scheduleAlarm(context, meetingTime.getTimeInMillis() - 2 * 60 * 60 * 1000, protocolNumber * 10 + 1);
        // За 15 минут
        scheduleAlarm(context, meetingTime.getTimeInMillis() - 15 * 60 * 1000, protocolNumber * 10 + 2);
    }

    private static void scheduleAlarm(Context context, long triggerTime, int requestCode) {
        Intent intent = new Intent(context, com.example.kafedrameetingapp.NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }
}
