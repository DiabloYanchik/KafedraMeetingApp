package com.example.kafedrameetingapp.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.kafedrameetingapp.utils.FirebaseUtils;

public class NotificationWorker extends Worker {
    private static final String TAG = "NotificationWorker";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "Checking scheduled notifications");
        FirebaseUtils.checkScheduledNotifications(new FirebaseUtils.Callback() {
            @Override
            public void onSuccess(String meetingId) {
                Log.d(TAG, "Successfully checked notifications");
            }

            @Override
            public void onSuccess(int protocolNumber) {}

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error checking notifications: " + e.getMessage());
            }
        });
        return Result.success();
    }
}