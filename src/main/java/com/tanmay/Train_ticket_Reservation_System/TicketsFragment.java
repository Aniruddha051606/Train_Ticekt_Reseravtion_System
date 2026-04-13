package com.tanmay.Train_ticket_Reservation_System;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketsFragment extends Fragment {

    private RecyclerView rvTickets;
    private ProgressBar progressBarTickets;
    private View tvNoTickets; // <-- FIXED: Changed from TextView to generic View

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tickets, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvTickets = view.findViewById(R.id.recyclerViewTickets);
        progressBarTickets = view.findViewById(R.id.progressBarTickets);
        tvNoTickets = view.findViewById(R.id.tvNoTickets);

        rvTickets.setLayoutManager(new LinearLayoutManager(requireContext()));

        fetchMyTickets();
    }

    private void fetchMyTickets() {
        // Grab the user's JWT token
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        if (token.isEmpty()) {
            Toast.makeText(getContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hit the Render API
        TrainApi api = RetrofitClient.getApiService();
        api.getMyTickets("Bearer " + token).enqueue(new Callback<List<TrainApi.Ticket>>() {
            @Override
            public void onResponse(Call<List<TrainApi.Ticket>> call, Response<List<TrainApi.Ticket>> response) {
                progressBarTickets.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<TrainApi.Ticket> myTickets = response.body();

                    if (myTickets.isEmpty()) {
                        tvNoTickets.setVisibility(View.VISIBLE);
                        rvTickets.setVisibility(View.GONE);
                    } else {
                        tvNoTickets.setVisibility(View.GONE);
                        rvTickets.setVisibility(View.VISIBLE);
                        rvTickets.setAdapter(new TicketAdapter(myTickets));
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load tickets", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TrainApi.Ticket>> call, Throwable t) {
                progressBarTickets.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}