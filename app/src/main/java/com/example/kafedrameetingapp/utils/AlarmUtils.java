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
        scheduleAlarm(context, meetingTime.getTimeInMillis() - 2 * 60 * 60 * 1000, meeting.protocolNumber * 100 + 1, meeting);
        scheduleAlarm(context, meetingTime.getTimeInMillis() - 15 * 60 * 1000, meeting.protocolNumber * 100 + 2, meeting);
    }

    private static void scheduleAlarm(Context context, long triggerTime, int requestCode, Meeting meeting) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("protocolNumber", meeting.protocolNumber);
        intent.putExtra("topic", meeting.topic);
        intent.putExtra("date", meeting.date);
        intent.putExtra("time", meeting.time);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.d(TAG, "Using inexact alarm due to lack of SCHEDULE_EXACT_ALARM permission");
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            } else {
                Log.d(TAG, "Scheduling exact alarm");
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            }
        } else {
            Log.e(TAG, "AlarmManager is null");
        }
    }
}