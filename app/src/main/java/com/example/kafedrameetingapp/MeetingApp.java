package com.example.kafedrameetingapp;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class MeetingApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
