package com.example.kafedrameetingapp.utils;

import com.example.kafedrameetingapp.models.Meeting;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseUtils {
    private static final DatabaseReference meetingsRef = FirebaseDatabase.getInstance().getReference("meetings");

    public static void saveMeeting(Meeting meeting, Callback callback) {
        String key = meetingsRef.push().getKey();
        if (key != null) {
            meeting.setId(key); // Сохраняем ID
            meetingsRef.child(key).setValue(meeting)
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(callback::onError);
        } else {
            callback.onError(new Exception("Не удалось сгенерировать ключ"));
        }
    }

    public static void loadMeetings(ValueEventListener listener) {
        meetingsRef.addValueEventListener(listener);
    }

    public static void deleteMeeting(String meetingId, Callback callback) {
        meetingsRef.child(meetingId).removeValue()
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