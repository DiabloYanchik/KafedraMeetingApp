package com.example.kafedrameetingapp.utils;

import androidx.annotation.NonNull;

import com.example.kafedrameetingapp.models.Meeting;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FirebaseUtils {
    private static final DatabaseReference meetingsRef = FirebaseDatabase.getInstance().getReference("meetings");
    private static final DatabaseReference archiveRef = FirebaseDatabase.getInstance().getReference("archive");

    public static void saveMeeting(Meeting meeting, Callback callback) {
        String key = meetingsRef.push().getKey();
        if (key != null) {
            meeting.setId(key);
            meetingsRef.child(key).setValue(meeting)
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(callback::onError);
        } else {
            callback.onError(new Exception("Не удалось сгенерировать ключ"));
        }
    }

    public static void loadMeetings(ValueEventListener listener) {
        meetingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                Calendar now = Calendar.getInstance();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Meeting meeting = data.getValue(Meeting.class);
                    if (meeting != null) {
                        meeting.setId(data.getKey());
                        try {
                            Date meetingDate = sdf.parse(meeting.date + " " + meeting.time);
                            if (meetingDate != null && meetingDate.before(now.getTime())) {
                                // Перемещаем в архив
                                archiveRef.child(meeting.getId()).setValue(meeting);
                                meetingsRef.child(meeting.getId()).removeValue();
                            }
                        } catch (Exception e) {
                            // Игнорируем ошибки парсинга
                        }
                    }
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
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public static void deleteArchiveMeeting(String meetingId, Callback callback) {
        archiveRef.child(meetingId).removeValue()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public static void getNextProtocolNumber(Callback callback) {
        meetingsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                callback.onSuccess((int) task.getResult().getChildrenCount() + 1);
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public interface Callback {
        void onSuccess();
        void onSuccess(int protocolNumber);
        void onError(Exception e);
    }
}