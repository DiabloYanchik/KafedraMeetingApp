package com.example.kafedrameetingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        EditText email = findViewById(R.id.editEmail);
        EditText password = findViewById(R.id.editPassword);
        Button login = findViewById(R.id.btnLogin);
        Button register = findViewById(R.id.btnRegister);

        login.setOnClickListener(v -> {
            String emailStr = email.getText().toString().trim();
            String passwordStr = password.getText().toString().trim();

            if (emailStr.isEmpty() || passwordStr.isEmpty()) {
                Toast.makeText(this, "Заполните email и пароль", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(emailStr, passwordStr)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Ошибка входа: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        register.setOnClickListener(v -> {
            String emailStr = email.getText().toString().trim();
            String passwordStr = password.getText().toString().trim();

            if (emailStr.isEmpty() || passwordStr.isEmpty()) {
                Toast.makeText(this, "Заполните email и пароль", Toast.LENGTH_SHORT).show();
                return;
            }

            if (passwordStr.length() < 6) {
                Toast.makeText(this, "Пароль должен быть не менее 6 символов", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(emailStr, passwordStr)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Регистрация успешна, пожалуйста, войдите", Toast.LENGTH_SHORT).show();
                            email.setText("");
                            password.setText("");
                        } else {
                            Toast.makeText(this, "Ошибка регистрации: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}