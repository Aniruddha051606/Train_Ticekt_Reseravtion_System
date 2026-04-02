package com.tanmay.Train_ticket_Reservation_System;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TrainAdapter extends RecyclerView.Adapter<TrainAdapter.TrainViewHolder> {

    private List<TrainApi.Train> trainList;

    public TrainAdapter(List<TrainApi.Train> trainList) {
        this.trainList = trainList;
    }

    @NonNull
    @Override
    public TrainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_train, parent, false);
        return new TrainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainViewHolder holder, int position) {
        TrainApi.Train train = trainList.get(position);

        holder.tvTrainName.setText(train.trainNumber + " - " + train.trainName);
        holder.tvTimeInfo.setText("Departs: " + train.departureTime + " | Arrives: " + train.arrivalTime);
        holder.tvPrice.setText("₹ " + train.price);

        // Handle Availability Colors
        holder.tvAvailability.setText(train.availability);
        if (train.availability.startsWith("AVL")) {
            holder.tvAvailability.setTextColor(Color.parseColor("#388E3C")); // Green
            holder.btnBook.setEnabled(true);
            holder.btnBook.setAlpha(1.0f);
        } else if (train.availability.startsWith("WL")) {
            holder.tvAvailability.setTextColor(Color.parseColor("#F57C00")); // Orange
            holder.btnBook.setEnabled(true);
            holder.btnBook.setAlpha(1.0f);
        } else {
            holder.tvAvailability.setTextColor(Color.parseColor("#D32F2F")); // Red
            holder.btnBook.setEnabled(false); // Disable button if REGRET/Sold Out
            holder.btnBook.setAlpha(0.5f);
        }

        // 🌟 THIS IS THE FIX: Open the Booking Page when clicked!
        holder.btnBook.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), BookingActivity.class);
            intent.putExtra("TRAIN_NUM", train.trainNumber);
            intent.putExtra("TRAIN_NAME", train.trainName);
            intent.putExtra("PRICE", train.price);

            // Passing default placeholders for now, these can be passed from SearchFragment later!
            intent.putExtra("DATE", "2026-05-12");
            intent.putExtra("SOURCE", "SELECTED_SOURCE");
            intent.putExtra("DEST", "SELECTED_DEST");

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return trainList.size();
    }

    public static class TrainViewHolder extends RecyclerView.ViewHolder {
        TextView tvTrainName, tvTimeInfo, tvAvailability, tvPrice;
        Button btnBook;

        public TrainViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTrainName = itemView.findViewById(R.id.tvTrainName);
            tvTimeInfo = itemView.findViewById(R.id.tvTimeInfo);
            tvAvailability = itemView.findViewById(R.id.tvAvailability);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnBook = itemView.findViewById(R.id.btnBook);
        }
    }
}