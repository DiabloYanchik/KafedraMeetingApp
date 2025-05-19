package com.example.kafedrameetingapp.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.kafedrameetingapp.models.Meeting;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FirebaseUtils {
    private static final DatabaseReference meetingsRef = FirebaseDatabase.getInstance().getReference("meetings");
    private static final DatabaseReference archiveRef = FirebaseDatabase.getInstance().getReference("archive");
    private static final DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");

    public static void saveMeeting(Meeting meeting, Callback callback) {
        String key = meetingsRef.push().getKey();
        if (key != null) {
            meeting.setId(key);
            meetingsRef.child(key).setValue(meeting)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("FirebaseUtils", "Meeting saved with ID: " + key);
                        callback.onSuccess(key);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseUtils", "Failed to save meeting: " + e.getMessage());
                        callback.onError(e);
                    });
        } else {
            Log.e("FirebaseUtils", "Failed to generate key for meeting");
            callback.onError(new Exception("Не удалось сгенерировать ключ"));
        }
    }

    public static void loadMeetings(ValueEventListener listener) {
        meetingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                Calendar now = Calendar.getInstance();

                List<String> meetingsToArchive = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Meeting meeting = data.getValue(Meeting.class);
                    if (meeting != null) {
                        meeting.setId(data.getKey());
                        try {
                            Date meetingDate = sdf.parse(meeting.date + " " + meeting.time);
                            if (meetingDate != null && meetingDate.before(now.getTime())) {
                                meetingsToArchive.add(meeting.getId());
                                archiveRef.child(meeting.getId()).setValue(meeting);
                            }
                        } catch (Exception e) {
                            Log.e("FirebaseUtils", "Ошибка парсинга даты: " + e.getMessage());
                        }
                    }
                }

                for (String meetingId : meetingsToArchive) {
                    meetingsRef.child(meetingId).removeValue();
                }

                listener.onDataChange(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onCancelled(error);
            }
        });
    }

    public static void loadArchiveMeetings(ValueEventListener listener) {
        archiveRef.addValueEventListener(listener);
    }

    public static void deleteMeeting(String meetingId, Callback callback) {
        meetingsRef.child(meetingId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    notificationsRef.child(meetingId).removeValue();
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onError);
    }

    public static void deleteArchiveMeeting(String meetingId, Callback callback) {
        archiveRef.child(meetingId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    notificationsRef.child(meetingId).removeValue();
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onError);
    }

    public static void getNextProtocolNumber(Callback callback) {
        Task<DataSnapshot> meetingsTask = meetingsRef.get();
        Task<DataSnapshot> archiveTask = archiveRef.get();

        Tasks.whenAllSuccess(meetingsTask, archiveTask).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                long meetingsCount = meetingsTask.getResult().getChildrenCount();
                long archiveCount = archiveTask.getResult().getChildrenCount();
                int nextProtocolNumber = (int) (meetingsCount + archiveCount + 1);
                callback.onSuccess(nextProtocolNumber);
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public static void updateMeeting(Meeting meeting, Callback callback) {
        if (meeting.getId() != null) {
            meetingsRef.child(meeting.getId()).setValue(meeting)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                    .addOnFailureListener(callback::onError);
        } else {
            callback.onError(new Exception("ID заседания не найдено"));
        }
    }

    public static void scheduleNotification(String meetingId, long triggerTime, String message) {
        if (meetingId == null) {
            Log.e("FirebaseUtils", "Meeting ID is null");
            return;
        }
        Log.d("FirebaseUtils", "Attempting to save notification: meetingId=" + meetingId + ", triggerTime=" + triggerTime + ", message=" + message);
        notificationsRef.child(meetingId).child(String.valueOf(triggerTime)).setValue(message)
                .addOnSuccessListener(aVoid -> Log.d("FirebaseUtils", "Notification saved successfully for meeting: " + meetingId))
                .addOnFailureListener(e -> Log.e("FirebaseUtils", "Failed to save notification: " + e.getMessage()));
    }

    public static void checkScheduledNotifications(Callback callback) {
        notificationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot meetingSnapshot : snapshot.getChildren()) {
                    String meetingId = meetingSnapshot.getKey();
                    for (DataSnapshot notificationSnapshot : meetingSnapshot.getChildren()) {
                        try {
                            long triggerTime = Long.parseLong(notificationSnapshot.getKey());
                            if (triggerTime <= System.currentTimeMillis()) {
                                Log.d("FirebaseUtils", "Outdated notification found for meeting: " + meetingId + " at " + triggerTime + ", will be removed by Cloud Functions");
                            }
                        } catch (NumberFormatException e) {
                            Log.e("FirebaseUtils", "Invalid triggerTime format for meeting: " + meetingId + ", key: " + notificationSnapshot.getKey());
                        }
                    }
                }
                callback.onSuccess(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    public interface Callback {
        void onSuccess(String meetingId);
        void onSuccess(int protocolNumber);
        void onError(Exception e);
    }
}