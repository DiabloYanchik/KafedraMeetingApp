package com.example.kafedrameetingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        EditText email = findViewById(R.id.editEmail);
        EditText password = findViewById(R.id.editPassword);
        Button login = findViewById(R.id.btnLogin);
        Button register = findViewById(R.id.btnRegister);

        // Автозаполнение полей из SharedPreferences
        String savedEmail = prefs.getString("email", "");
        String savedPassword = prefs.getString("password", "");
        email.setText(savedEmail);
        password.setText(savedPassword);

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
                            // Сохраняем учетные данные
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("email", emailStr);
                            editor.putString("password", passwordStr);
                            editor.apply();

                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            // Проверяем тип ошибки
                            Exception e = task.getException();
                            if (e instanceof FirebaseAuthInvalidCredentialsException ||
                                    e instanceof FirebaseAuthInvalidUserException) {
                                Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Ошибка входа: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
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
                            // Сохраняем учетные данные
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("email", emailStr);
                            editor.putString("password", passwordStr);
                            editor.apply();

                            // Автоматический вход
                            mAuth.signInWithEmailAndPassword(emailStr, passwordStr)
                                    .addOnCompleteListener(signInTask -> {
                                        if (signInTask.isSuccessful()) {
                                            Toast.makeText(this, "Регистрация и вход успешны", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(this, MainActivity.class));
                                            finish();
                                        } else {
                                            // Проверяем тип ошибки при входе после регистрации
                                            Exception e = signInTask.getException();
                                            if (e instanceof FirebaseAuthInvalidCredentialsException ||
                                                    e instanceof FirebaseAuthInvalidUserException) {
                                                Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(this, "Ошибка входа после регистрации: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(this, "Ошибка регистрации: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}