package com.tanmay.Train_ticket_Reservation_System;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import java.util.ArrayList;

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
        String name = etPassengerName.getText() != null ? etPassengerName.getText().toString().trim() : "";
        String ageStr = etPassengerAge.getText() != null ? etPassengerAge.getText().toString().trim() : "";
        String gender = etPassengerGender.getText() != null ? etPassengerGender.getText().toString().trim() : "";
        String email = etContactEmail.getText() != null ? etContactEmail.getText().toString().trim() : "";

        if (name.isEmpty() || ageStr.isEmpty() || gender.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show();
            return;
        }

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

        // 2. Package it up and send to Payment Activity using Gson
        String requestJson = new Gson().toJson(request);

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("BOOKING_REQUEST", requestJson);
        startActivity(intent);
    }
}