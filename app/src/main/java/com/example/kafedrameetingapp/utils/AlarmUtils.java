package com.example.kafedrameetingapp.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.kafedrameetingapp.models.Meeting;
import com.example.kafedrameetingapp.work.NotificationReceiver;

import java.util.Calendar;

public class AlarmUtils {
    private static final String TAG = "AlarmUtils";

    public static void scheduleAlarms(Context context, Calendar meetingTime, Meeting meeting) {
        scheduleLocalAlarm(context, meetingTime.getTimeInMillis() - 2 * 60 * 1000, meeting.getProtocolNumber() * 100 + 1, meeting, "2 минуты");
        scheduleLocalAlarm(context, meetingTime.getTimeInMillis() - 1 * 60 * 1000, meeting.getProtocolNumber() * 100 + 2, meeting, "1 минута");
        scheduleFCMNotification(meeting, meetingTime.getTimeInMillis() - 2 * 60 * 1000, "2 минуты");
        scheduleFCMNotification(meeting, meetingTime.getTimeInMillis() - 1 * 60 * 1000, "1 минута");
    }

    private static void scheduleLocalAlarm(Context context, long triggerTime, int requestCode, Meeting meeting, String timeBefore) {
        if (triggerTime < System.currentTimeMillis()) {
            Log.d(TAG, "Время уведомления уже прошло, пропускаем: " + requestCode);
            return;
        }

        String message = "Заседание скоро начнется (за " + timeBefore + ").\n" +
                "Тема: " + (meeting.getTopic() != null ? meeting.getTopic() : "Не указано") + "\n" +
                "Дата: " + (meeting.getDate() != null ? meeting.getDate() : "") + " " +
                (meeting.getTime() != null ? meeting.getTime() : "") +
                (meeting.getProtocolNumber() != 0 ? "\nПротокол №" + meeting.getProtocolNumber() : "") +
                (meeting.getRoomNumber() != null && !meeting.getRoomNumber().isEmpty() ?
                        "\nКабинет: " + meeting.getRoomNumber() : "");

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("title", "Скоро заседание кафедры!");
        intent.putExtra("message", message);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.d(TAG, "Using inexact alarm due to lack of SCHEDULE_EXACT_ALARM permission");
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                Log.d(TAG, "Scheduling exact alarm for requestCode: " + requestCode);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        } else {
            Log.e(TAG, "AlarmManager is null");
        }
    }

    private static void scheduleFCMNotification(Meeting meeting, long triggerTime, String timeBefore) {
        if (triggerTime < System.currentTimeMillis()) {
            Log.d(TAG, "Время уведомления уже прошло, пропускаем: " + meeting.getTopic());
            return;
        }
        if (meeting.getId() == null) {
            Log.e(TAG, "Meeting ID is null for meeting: " + meeting.getTopic());
            return;
        }

        String message = "Заседание скоро начнется (за " + timeBefore + ").\n" +
                "Тема: " + (meeting.getTopic() != null ? meeting.getTopic() : "Не указано") + "\n" +
                "Дата: " + (meeting.getDate() != null ? meeting.getDate() : "") + " " +
                (meeting.getTime() != null ? meeting.getTime() : "") +
                (meeting.getProtocolNumber() != 0 ? "\nПротокол №" + meeting.getProtocolNumber() : "") +
                (meeting.getRoomNumber() != null && !meeting.getRoomNumber().isEmpty() ?
                        "\nКабинет: " + meeting.getRoomNumber() : "");

        FirebaseUtils.scheduleNotification(meeting.getId(), triggerTime, message);
        Log.d(TAG, "Scheduled FCM notification for meeting: " + meeting.getTopic() + " at " + triggerTime);
    }
}