package com.tanmay.Train_ticket_Reservation_System;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.Gson;
import java.util.UUID;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvPaymentAmount;
    private Button btnPayNow;
    private TrainApi.BookTicketRequest requestData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        tvPaymentAmount = findViewById(R.id.tvPaymentAmount);
        btnPayNow = findViewById(R.id.btnPayNow);

        // 1. Receive the booking data from BookingActivity
        String requestJson = getIntent().getStringExtra("BOOKING_REQUEST");
        requestData = new Gson().fromJson(requestJson, TrainApi.BookTicketRequest.class);

        tvPaymentAmount.setText("Total Amount: ₹ " + requestData.totalPrice);

        btnPayNow.setOnClickListener(v -> processFakePayment());
    }

    private void processFakePayment() {
        btnPayNow.setText("Processing Payment...");
        btnPayNow.setEnabled(false);

        // Simulate a 2-second banking delay for realism
        new Handler(Looper.getMainLooper()).postDelayed(this::executeBookingTransaction, 2000);
    }

    private void executeBookingTransaction() {
        btnPayNow.setText("Confirming Seats...");

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        // 🌟 FAANG FEATURE: Generate Idempotency Key
        String idempotencyKey = UUID.randomUUID().toString();

        TrainApi api = RetrofitClient.getApiService();
        api.bookTicket("Bearer " + token, idempotencyKey, requestData).enqueue(new Callback<TrainApi.BookingResponse>() {
            @Override
            public void onResponse(Call<TrainApi.BookingResponse> call, Response<TrainApi.BookingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(PaymentActivity.this, "✅ Payment Successful! PNR: " + response.body().pnr, Toast.LENGTH_LONG).show();

                    // Go back to Dashboard
                    Intent intent = new Intent(PaymentActivity.this, DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    btnPayNow.setText("Pay Securely");
                    btnPayNow.setEnabled(true);
                    Toast.makeText(PaymentActivity.this, "❌ Server Error: " + response.code(), Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(Call<TrainApi.BookingResponse> call, Throwable t) {
                btnPayNow.setText("Pay Securely");
                btnPayNow.setEnabled(true);
                Toast.makeText(PaymentActivity.this, "Network Error", Toast.LENGTH_LONG).show();
            }
        });
    }
}