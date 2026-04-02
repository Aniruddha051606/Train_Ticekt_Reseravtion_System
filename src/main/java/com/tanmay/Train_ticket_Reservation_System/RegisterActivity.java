package com.tanmay.Train_ticket_Reservation_System;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnRegister;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // When they click Register, send data to the server
        btnRegister.setOnClickListener(v -> attemptRegister());

        // When they click Login, just close this screen to go back
        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        TrainApi api = RetrofitClient.getApiService();
        api.register(new TrainApi.RegisterRequest(name, email, password)).enqueue(new Callback<TrainApi.AuthResponse>() {
            @Override
            public void onResponse(Call<TrainApi.AuthResponse> call, Response<TrainApi.AuthResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "✅ Account Created! Please Login.", Toast.LENGTH_LONG).show();
                    finish(); // Automatically go back to the Login screen
                } else {
                    Toast.makeText(RegisterActivity.this, "❌ Email might already be in use.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TrainApi.AuthResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}