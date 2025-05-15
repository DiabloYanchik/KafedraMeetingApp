package com.example.kafedrameetingapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.Manifest;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;


public class MainActivity extends AppCompatActivity {
Button btnCreate;
RecyclerView recyclerView;
MeetingAdapter adapter;

    @Override
    protected void onResume() {
        super.onResume();
        List<Meeting> meetings = JsonUtils.loadMeetings(this);
        adapter = new MeetingAdapter(meetings);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        //


        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001);
            }
        }

        btnCreate = findViewById(R.id.btnCreateMeeting);
        recyclerView = findViewById(R.id.recyclerViewMeetings);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnCreate.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateMeetingActivity.class);
            startActivity(intent);
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение получено — можно показывать уведомления
            } else {
                Toast.makeText(this, "Разрешение на уведомления не предоставлено", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //

}