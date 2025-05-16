package com.example.kafedrameetingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kafedrameetingapp.adapters.MeetingAdapter;
import com.example.kafedrameetingapp.models.Meeting;
import com.example.kafedrameetingapp.utils.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ArchiveActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    MeetingAdapter adapter;
    List<Meeting> meetings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);

        recyclerView = findViewById(R.id.recyclerViewArchive);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        meetings = new ArrayList<>();
        adapter = new MeetingAdapter(meetings, meeting -> {
            Intent intent = new Intent(ArchiveActivity.this, MeetingDetailActivity.class);
            intent.putExtra("meeting", meeting);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        FirebaseUtils.loadArchiveMeetings(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                meetings.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Meeting meeting = data.getValue(Meeting.class);
                    if (meeting != null) {
                        meeting.setId(data.getKey());
                        meetings.add(meeting);
                    }
                }
                Collections.sort(meetings, (m1, m2) -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                    try {
                        return sdf.parse(m1.date + " " + m1.time).compareTo(sdf.parse(m2.date + " " + m2.time));
                    } catch (ParseException e) {
                        return 0;
                    }
                });
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ArchiveActivity.this, "Ошибка загрузки архива: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}