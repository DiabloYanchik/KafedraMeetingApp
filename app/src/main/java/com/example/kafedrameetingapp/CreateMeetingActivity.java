package com.example.kafedrameetingapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

public class CreateMeetingActivity extends AppCompatActivity {
    EditText editTopic, editAgenda, editDate, editTime;
    Button btnSave;

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format("%02d.%02d.%04d", selectedDay, selectedMonth + 1, selectedYear);
                    editDate.setText(formattedDate);
                },
                year, month, day);

        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    String formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                    editTime.setText(formattedTime);
                },
                hour, minute, true); // true — 24-часовой формат

        timePickerDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meeting);

        editTopic = findViewById(R.id.editTopic);
        editAgenda = findViewById(R.id.editAgenda);
        editDate = findViewById(R.id.editDate);
        editTime = findViewById(R.id.editTime);
        btnSave = findViewById(R.id.btnSave);
//
        editDate.setOnClickListener(v -> showDatePicker());
        editTime.setOnClickListener(v -> showTimePicker());

        //
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
            }
        }

        btnSave.setOnClickListener(v -> {
            String topic = editTopic.getText().toString();
            String agenda = editAgenda.getText().toString();
            String date = editDate.getText().toString();
            String time = editTime.getText().toString();
            int protocol = JsonUtils.getNextProtocolNumber(this);

            Meeting meeting = new Meeting(topic, agenda, date, time, protocol);
            JsonUtils.saveMeeting(this, meeting);

            Calendar calendar = Calendar.getInstance();
            String[] dateParts = date.split("\\.");
            String[] timeParts = time.split(":");
            calendar.set(Calendar.YEAR, Integer.parseInt(dateParts[2]));
            calendar.set(Calendar.MONTH, Integer.parseInt(dateParts[1]) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateParts[0]));
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
            calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));


            // Планируем уведомления
            new Thread(() -> {
                AlarmUtils.scheduleAlarms(this, calendar, protocol);
                // Завершаем активность на главном потоке
                runOnUiThread(() -> finish());
            }).start();
        });





    }
}

