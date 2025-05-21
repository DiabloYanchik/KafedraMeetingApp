package com.example.kafedrameetingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kafedrameetingapp.adapters.MeetingAdapter;
import com.example.kafedrameetingapp.models.Meeting;
import com.example.kafedrameetingapp.utils.FirebaseUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ArchiveActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    MeetingAdapter adapter;
    List<Meeting> meetings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ArchiveActivity", "Starting ArchiveActivity");
        setContentView(R.layout.activity_archive);
        setTitle("Архив заседаний");

        recyclerView = findViewById(R.id.recyclerViewArchive);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        meetings = new ArrayList<>();
        adapter = new MeetingAdapter(meetings, meeting -> {
            Log.d("ArchiveActivity", "Clicked meeting: " + meeting.getTopic());
            Intent intent = new Intent(ArchiveActivity.this, MeetingDetailActivity.class);
            intent.putExtra("meeting", meeting);
            intent.putExtra("isArchived", true);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        FirebaseUtils.loadArchiveMeetings(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Log.d("ArchiveActivity", "Archive data loaded: " + snapshot.getChildrenCount() + " items");
                meetings.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Meeting meeting = data.getValue(Meeting.class);
                    if (meeting != null) {
                        meeting.setId(data.getKey());
                        meetings.add(meeting);
                        Log.d("ArchiveActivity", "Loaded meeting: " + meeting.getTopic());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("ArchiveActivity", "Error loading archive: " + error.getMessage());
                Toast.makeText(ArchiveActivity.this, "Ошибка загрузки архива: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}