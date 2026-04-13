package com.tanmay.Train_ticket_Reservation_System;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private AutoCompleteTextView etSource, etDestination;
    private EditText etDate;
    private Button btnSearch;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TrainAdapter adapter;
    private TrainApi trainApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            etSource = view.findViewById(R.id.etSource);
            etDestination = view.findViewById(R.id.etDestination);
            etDate = view.findViewById(R.id.etDate);
            btnSearch = view.findViewById(R.id.btnSearch);
            recyclerView = view.findViewById(R.id.recyclerView);
            progressBar = view.findViewById(R.id.progressBar);

            // Fix: Dynamic User Greeting
            TextView tvGreeting = view.findViewById(R.id.tvGreeting);
            SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            // Check for "name" or "user_name" depending on how your Login saves it
            String userName = prefs.getString("name", prefs.getString("user_name", "Passenger"));
            if (tvGreeting != null) {
                String firstName = userName.split(" ")[0]; // Get first name only
                tvGreeting.setText("Namaste, " + firstName + "!\nWhere are you going today?");
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            trainApi = RetrofitClient.getApiService();

            ArrayAdapter<TrainApi.Station> sourceAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line);
            ArrayAdapter<TrainApi.Station> destAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line);
            etSource.setAdapter(sourceAdapter);
            etDestination.setAdapter(destAdapter);

            setupLiveSearch(etSource, sourceAdapter);
            setupLiveSearch(etDestination, destAdapter);

            etDate.setOnClickListener(v -> showDatePicker());
            btnSearch.setOnClickListener(v -> performSearch());
        } catch (Exception e) {
            Log.e("SearchFragment", "UI Setup Error", e);
        }
    }

    private void setupLiveSearch(AutoCompleteTextView textView, ArrayAdapter<TrainApi.Station> adapter) {
        textView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 2 && !s.toString().contains(" - ")) {
                    trainApi.searchStations(s.toString()).enqueue(new Callback<List<TrainApi.Station>>() {
                        @Override
                        public void onResponse(Call<List<TrainApi.Station>> call, Response<List<TrainApi.Station>> response) {
                            if (!isAdded() || getContext() == null) return;
                            if (response.isSuccessful() && response.body() != null) {
                                adapter.clear();
                                adapter.addAll(response.body());
                                adapter.notifyDataSetChanged();
                            }
                        }
                        @Override
                        public void onFailure(Call<List<TrainApi.Station>> call, Throwable t) {}
                    });
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            etDate.setText(String.format("%d-%02d-%02d", year, month + 1, day));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String extractStationCode(String rawInput) {
        if (rawInput.contains(" - ")) {
            String[] parts = rawInput.split(" - ");
            return parts[parts.length - 1].trim().toUpperCase();
        }
        return rawInput.trim().toUpperCase();
    }

    private void performSearch() {
        try {
            String rawSource = etSource.getText().toString().trim();
            String rawDest = etDestination.getText().toString().trim();
            String date = etDate.getText().toString().trim();

            if (rawSource.isEmpty() || rawDest.isEmpty() || date.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String sourceCode = extractStationCode(rawSource);
            String destCode = extractStationCode(rawDest);

            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

            trainApi.getTrains(sourceCode, destCode, date).enqueue(new Callback<List<TrainApi.Train>>() {
                @Override
                public void onResponse(Call<List<TrainApi.Train>> call, Response<List<TrainApi.Train>> response) {
                    if (!isAdded() || getContext() == null) return;

                    progressBar.setVisibility(View.GONE);

                    if (response.isSuccessful() && response.body() != null) {
                        List<TrainApi.Train> trainList = response.body();
                        if (trainList.isEmpty()) {
                            Toast.makeText(getContext(), "No trains found for this route", Toast.LENGTH_LONG).show();
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            adapter = new TrainAdapter(trainList, sourceCode, destCode, date);
                            recyclerView.setAdapter(adapter);
                        }
                    } else {
                        Toast.makeText(getContext(), "Server error or no trains available.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<TrainApi.Train>> call, Throwable t) {
                    if (!isAdded() || getContext() == null) return;

                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Connection failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Log.e("SearchFragment", "Search Crash", e);
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}