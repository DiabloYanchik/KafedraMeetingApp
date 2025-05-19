package com.example.kafedrameetingapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kafedrameetingapp.models.Meeting;
import com.example.kafedrameetingapp.utils.AlarmUtils;
import com.example.kafedrameetingapp.utils.FirebaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;

public class CreateMeetingActivity extends AppCompatActivity {
    private static final String TAG = "CreateMeetingActivity";
    private EditText editTopic, editAgenda, editDate, editTime, editRoomNumber;
    private Button btnSave;
    private Meeting existingMeeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meeting);

        editTopic = findViewById(R.id.editTopic);
        editAgenda = findViewById(R.id.editAgenda);
        editDate = findViewById(R.id.editDate);
        editTime = findViewById(R.id.editTime);
        editRoomNumber = findViewById(R.id.editRoomNumber);
        btnSave = findViewById(R.id.btnSave);

        existingMeeting = (Meeting) getIntent().getSerializableExtra("meeting");
        if (existingMeeting != null) {
            editTopic.setText(existingMeeting.getTopic());
            editAgenda.setText(existingMeeting.getAgenda());
            editDate.setText(existingMeeting.getDate());
            editTime.setText(existingMeeting.getTime());
            editRoomNumber.setText(existingMeeting.getRoomNumber());
        }

        btnSave.setOnClickListener(v -> {
            String topic = editTopic.getText().toString().trim();
            String agenda = editAgenda.getText().toString().trim();
            String date = editDate.getText().toString();
            String time = editTime.getText().toString();
            String roomNumber = editRoomNumber.getText().toString().trim();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null || !user.getEmail().equals("yaniayurieva@gmail.com")) {
                Log.e(TAG, "User not authorized: " + (user == null ? "null" : user.getEmail()));
                Toast.makeText(this, "Только администратор может создавать заседания", Toast.LENGTH_SHORT).show();
                return;
            }

            if (topic.isEmpty() || date.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
                return;
            }

            Calendar calendar = Calendar.getInstance();
            String[] dateParts = date.split("\\.");
            String[] timeParts = time.split(":");
            try {
                calendar.set(Calendar.YEAR, Integer.parseInt(dateParts[2]));
                calendar.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1);
                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[0]));
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
                calendar.set(Calendar.SECOND, 0);
            } catch (Exception e) {
                Toast.makeText(this, "Неверный формат даты или времени", Toast.LENGTH_SHORT).show();
                return;
            }

            if (existingMeeting != null) {
                existingMeeting.setTopic(topic);
                existingMeeting.setAgenda(agenda);
                existingMeeting.setDate(date);
                existingMeeting.setTime(time);
                existingMeeting.setRoomNumber(roomNumber.isEmpty() ? null : roomNumber);
                FirebaseUtils.updateMeeting(existingMeeting, new FirebaseUtils.Callback() {
                    @Override
                    public void onSuccess(String meetingId) {
                        Log.d(TAG, "Meeting updated: " + existingMeeting.getId());
                        AlarmUtils.scheduleAlarms(CreateMeetingActivity.this, calendar, existingMeeting);
                        Toast.makeText(CreateMeetingActivity.this, "Заседание обновлено", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onSuccess(int protocolNumber) {}

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Failed to update meeting: " + e.getMessage());
                        Toast.makeText(CreateMeetingActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                FirebaseUtils.getNextProtocolNumber(new FirebaseUtils.Callback() {
                    @Override
                    public void onSuccess(int protocolNumber) {
                        Meeting newMeeting = new Meeting(topic, agenda, date, time, protocolNumber, roomNumber.isEmpty() ? null : roomNumber);
                        FirebaseUtils.saveMeeting(newMeeting, new FirebaseUtils.Callback() {
                            @Override
                            public void onSuccess(String meetingId) {
                                Log.d(TAG, "Meeting saved with ID: " + meetingId);
                                newMeeting.setId(meetingId); // Устанавливаем ID
                                AlarmUtils.scheduleAlarms(CreateMeetingActivity.this, calendar, newMeeting);
                                Toast.makeText(CreateMeetingActivity.this, "Заседание сохранено", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onSuccess(int ignored) {}

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Failed to save meeting: " + e.getMessage());
                                Toast.makeText(CreateMeetingActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onSuccess(String meetingId) {}

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Failed to get protocol number: " + e.getMessage());
                        Toast.makeText(CreateMeetingActivity.this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}