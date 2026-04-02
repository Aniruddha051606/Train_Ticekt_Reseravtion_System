package com.tanmay.Train_ticket_Reservation_System;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleLogin;
    private TextView tvRegister;
    private GoogleSignInClient mGoogleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    handleSignInResult(task);
                } else {
                    Toast.makeText(this, "Google Sign-In canceled.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Auto-login check! If they already have a token, skip the login screen
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        if (!prefs.getString("jwt_token", "").isEmpty()) {
            navigateToDashboard();
            return;
        }

        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        tvRegister = findViewById(R.id.tvRegister);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("907938777178-ajskfn1vjmesh7ovi46lh42n5jn2qvds.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin.setOnClickListener(v -> attemptLogin());

        btnGoogleLogin.setOnClickListener(v -> {
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            sendGoogleTokenToServer(account.getIdToken());
        } catch (ApiException e) {
            Log.e("GoogleAuth", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Google Auth Failed.", Toast.LENGTH_LONG).show();
        }
    }

    private void sendGoogleTokenToServer(String idToken) {
        TrainApi api = RetrofitClient.getApiService();
        api.googleLogin(new TrainApi.GoogleLoginRequest(idToken)).enqueue(new Callback<TrainApi.AuthResponse>() {
            @Override
            public void onResponse(Call<TrainApi.AuthResponse> call, Response<TrainApi.AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveSessionAndProceed(response.body());
                } else {
                    Toast.makeText(MainActivity.this, "❌ Server rejected Google token", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<TrainApi.AuthResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) return;

        TrainApi api = RetrofitClient.getApiService();
        api.login(new TrainApi.LoginRequest(email, password)).enqueue(new Callback<TrainApi.AuthResponse>() {
            @Override
            public void onResponse(Call<TrainApi.AuthResponse> call, Response<TrainApi.AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveSessionAndProceed(response.body());
                } else {
                    Toast.makeText(MainActivity.this, "❌ Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<TrainApi.AuthResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Helper method to save user data
    private void saveSessionAndProceed(TrainApi.AuthResponse authData) {
        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit()
                .putString("jwt_token", authData.token)
                .putString("user_name", authData.user.name)
                .putString("user_email", authData.user.email)
                .apply();

        Toast.makeText(this, "✅ Welcome, " + authData.user.name + "!", Toast.LENGTH_SHORT).show();
        navigateToDashboard();
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}