package com.tanmay.Train_ticket_Reservation_System;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingActivity extends AppCompatActivity {

    private String trainNumber, trainName, date, source, destination;
    private int ticketPrice;

    private TextView tvBookingTrainName, tvBookingDate, tvSource, tvDest, tvFinalPrice;
    private EditText etPassengerName, etPassengerAge, etPassengerGender, etContactEmail;
    private Button btnConfirmBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Map the new premium UI
        tvBookingTrainName = findViewById(R.id.tvBookingTrainName);
        tvBookingDate = findViewById(R.id.tvBookingDate);
        tvSource = findViewById(R.id.tvSource);
        tvDest = findViewById(R.id.tvDest);
        tvFinalPrice = findViewById(R.id.tvFinalPrice);

        etPassengerName = findViewById(R.id.etPassengerName);
        etPassengerAge = findViewById(R.id.etPassengerAge);
        etPassengerGender = findViewById(R.id.etPassengerGender);
        etContactEmail = findViewById(R.id.etContactEmail);
        btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        // Get Data from Intent
        trainNumber = getIntent().getStringExtra("TRAIN_NUM");
        trainName = getIntent().getStringExtra("TRAIN_NAME");
        date = getIntent().getStringExtra("DATE");
        source = getIntent().getStringExtra("SOURCE");
        destination = getIntent().getStringExtra("DEST");
        ticketPrice = getIntent().getIntExtra("PRICE", 0);

        // Set UI Text
        tvBookingTrainName.setText(trainNumber + " - " + trainName);
        tvBookingDate.setText("Journey Date: " + date);
        tvSource.setText(source);
        tvDest.setText(destination);
        tvFinalPrice.setText("₹ " + ticketPrice);

        btnConfirmBooking.setOnClickListener(v -> confirmBooking());
    }

    private void confirmBooking() {
        String name = etPassengerName.getText().toString().trim();
        String ageStr = etPassengerAge.getText().toString().trim();
        String gender = etPassengerGender.getText().toString().trim();
        String email = etContactEmail.getText().toString().trim();

        if (name.isEmpty() || ageStr.isEmpty() || gender.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show();
            return;
        }

        btnConfirmBooking.setText("Processing...");
        btnConfirmBooking.setEnabled(false);

        // 1. Prepare Request Data
        TrainApi.BookTicketRequest request = new TrainApi.BookTicketRequest();
        request.trainNumber = trainNumber;
        request.trainName = trainName;
        request.source = source;
        request.destination = destination;
        request.date = date;
        request.totalPrice = ticketPrice;

        TrainApi.Passenger passenger = new TrainApi.Passenger();
        passenger.name = name;
        passenger.age = Integer.parseInt(ageStr);
        passenger.gender = gender;

        request.passengers = new ArrayList<>();
        request.passengers.add(passenger);

        // 2. We need the user's JWT token to book! (Replace this with however you save your token)
        // For testing, if you aren't saving the token yet, the Node.js server might reject it.
        // Make sure your MainActivity saves the token to SharedPreferences upon login.
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        // 3. Make the API Call to Node.js!
        TrainApi api = RetrofitClient.getApiService();
        api.bookTicket("Bearer " + token, request).enqueue(new Callback<TrainApi.BookingResponse>() {
            @Override
            public void onResponse(Call<TrainApi.BookingResponse> call, Response<TrainApi.BookingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Success! The seats have been subtracted in MongoDB!
                    String pnr = response.body().pnr;
                    Toast.makeText(BookingActivity.this, "✅ Booked! PNR: " + pnr, Toast.LENGTH_LONG).show();
                    finish(); // Close booking screen and return to Dashboard
                } else {
                    btnConfirmBooking.setText("Proceed");
                    btnConfirmBooking.setEnabled(true);
                    Toast.makeText(BookingActivity.this, "❌ Booking Failed. Session expired?", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<TrainApi.BookingResponse> call, Throwable t) {
                btnConfirmBooking.setText("Proceed");
                btnConfirmBooking.setEnabled(true);
                Toast.makeText(BookingActivity.this, "Network Error", Toast.LENGTH_LONG).show();
            }
        });
    }
}