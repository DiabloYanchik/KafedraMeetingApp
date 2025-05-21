package com.example.kafedrameetingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.kafedrameetingapp.adapters.MeetingAdapter;
import com.example.kafedrameetingapp.models.Meeting;
import com.example.kafedrameetingapp.utils.FirebaseUtils;
import com.example.kafedrameetingapp.work.NotificationWorker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Button btnCreate, btnArchive, btnLogout;
    RecyclerView recyclerView;
    MeetingAdapter adapter;
    List<Meeting> meetings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Запрос разрешения на уведомления
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        1001);
            } else {
                Log.d(TAG, "POST_NOTIFICATIONS permission already granted");
            }
        }

        btnCreate = findViewById(R.id.btnCreateMeeting);
        btnArchive = findViewById(R.id.btnArchive);
        btnLogout = findViewById(R.id.btnLogout);
        recyclerView = findViewById(R.id.recyclerViewMeetings);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        meetings = new ArrayList<>();
        adapter = new MeetingAdapter(meetings, meeting -> {
            Intent intent = new Intent(MainActivity.this, MeetingDetailActivity.class);
            intent.putExtra("meeting", meeting);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        FirebaseUtils.loadMeetings(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "DataSnapshot: " + snapshot.toString());
                meetings.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Meeting meeting = data.getValue(Meeting.class);
                    if (meeting != null) {
                        meeting.setId(data.getKey());
                        meetings.add(meeting);
                        Log.d(TAG, "Meeting loaded: " + meeting.getTopic());
                    }
                }
                Collections.sort(meetings, (m1, m2) -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                    try {
                        return sdf.parse(m1.date + " " + m1.time).compareTo(sdf.parse(m2.date + " " + m2.time));
                    } catch (ParseException e) {
                        Log.e(TAG, "Ошибка сортировки: " + e.getMessage());
                        return 0;
                    }
                });
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Ошибка загрузки: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Планируем периодическую проверку уведомлений
        PeriodicWorkRequest notificationCheckRequest =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 15, TimeUnit.MINUTES)
                        .build();
        WorkManager.getInstance(this).enqueue(notificationCheckRequest);
        Log.d(TAG, "Scheduled periodic notification check");

        // Подписка на FCM
        subscribeToFCMTopic();

        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateMeetingActivity.class);
            startActivity(intent);
        });

        btnArchive.setOnClickListener(v -> {
            Intent intent = new Intent(this, ArchiveActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            // Отписка от FCM и удаление токена
            FirebaseMessaging.getInstance().unsubscribeFromTopic("meetings")
                    .addOnCompleteListener(task -> {
                        Log.d(TAG, "Unsubscribed from meetings topic: " + task.isSuccessful());
                    });
            FirebaseMessaging.getInstance().deleteToken()
                    .addOnCompleteListener(task -> {
                        Log.d(TAG, "FCM token deleted: " + task.isSuccessful());
                    });
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail().equals("yaniayurieva@gmail.com")) {
            btnCreate.setVisibility(View.VISIBLE);
        } else {
            btnCreate.setVisibility(View.GONE);
        }
    }

    private void subscribeToFCMTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("meetings")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Subscribed to meetings topic");
                        Toast.makeText(MainActivity.this, "Подписка на уведомления успешна", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "Failed to subscribe to meetings topic: " + task.getException());
                        Toast.makeText(MainActivity.this, "Ошибка подписки на уведомления", Toast.LENGTH_SHORT).show();
                    }
                });
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "FCM Token: " + task.getResult());
                    } else {
                        Log.e(TAG, "Failed to get FCM Token: " + task.getException());
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "POST_NOTIFICATIONS permission granted");
                Toast.makeText(this, "Разрешение на уведомления получено", Toast.LENGTH_SHORT).show();
                subscribeToFCMTopic();
            } else {
                Log.w(TAG, "POST_NOTIFICATIONS permission denied");
                Toast.makeText(this, "Разрешение на уведомления не предоставлено", Toast.LENGTH_SHORT).show();
            }
        }
    }
}