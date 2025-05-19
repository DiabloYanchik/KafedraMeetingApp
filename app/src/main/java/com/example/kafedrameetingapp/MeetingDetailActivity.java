package com.example.kafedrameetingapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kafedrameetingapp.models.Meeting;
import com.example.kafedrameetingapp.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;

public class MeetingDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_detail);

        Meeting meeting = (Meeting) getIntent().getSerializableExtra("meeting");

        TextView topic = findViewById(R.id.detailTopic);
        TextView agenda = findViewById(R.id.detailAgenda);
        TextView date = findViewById(R.id.detailDate);
        TextView time = findViewById(R.id.detailTime);
        TextView protocol = findViewById(R.id.detailProtocol);
        TextView roomNumber = findViewById(R.id.detailRoomNumber);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnDelete = findViewById(R.id.btnDelete);

        topic.setText("Тема: " + (meeting.topic != null ? meeting.topic : ""));
        agenda.setText("Повестка дня: " + (meeting.agenda != null ? meeting.agenda : ""));
        date.setText("Дата: " + (meeting.date != null ? meeting.date : ""));
        time.setText("Время: " + (meeting.time != null ? meeting.time : ""));
        protocol.setText("Протокол №: " + meeting.protocolNumber);
        roomNumber.setText("Кабинет: " + (meeting.roomNumber != null && !meeting.roomNumber.isEmpty() ? meeting.roomNumber : "Не указан"));

        boolean isArchived = getIntent().getBooleanExtra("isArchived", false);

        String userEmail = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
        if (userEmail != null && userEmail.equals("yaniayurieva@gmail.com")) {
            btnEdit.setVisibility(isArchived ? View.GONE : View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        }

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateMeetingActivity.class);
            intent.putExtra("meeting", meeting);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> {
            if (meeting.getId() != null) {
                FirebaseUtils.Callback callback = new FirebaseUtils.Callback() {
                    @Override
                    public void onSuccess(String meetingId) {
                        Toast.makeText(MeetingDetailActivity.this, "Заседание удалено", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onSuccess(int protocolNumber) {
                        // Не используется для удаления
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(MeetingDetailActivity.this, "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                };

                if (isArchived) {
                    FirebaseUtils.deleteArchiveMeeting(meeting.getId(), callback);
                } else {
                    FirebaseUtils.deleteMeeting(meeting.getId(), callback);
                }
            } else {
                Toast.makeText(this, "Ошибка: ID заседания не найдено", Toast.LENGTH_SHORT).show();
            }
        });
    }
}