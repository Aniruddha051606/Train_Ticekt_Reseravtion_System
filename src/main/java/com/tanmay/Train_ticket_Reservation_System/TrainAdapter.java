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

    private final List<TrainApi.Train> trainList;
    private final String source;
    private final String dest;
    private final String date;

    public TrainAdapter(List<TrainApi.Train> trainList, String source, String dest, String date) {
        this.trainList = trainList;
        this.source = source;
        this.dest = dest;
        this.date = date;
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

        holder.tvAvailability.setText(train.availability);
        if (train.availability.startsWith("AVL")) {
            holder.tvAvailability.setTextColor(Color.parseColor("#388E3C"));
            holder.btnBook.setEnabled(true);
            holder.btnBook.setAlpha(1.0f);
        } else if (train.availability.startsWith("WL")) {
            holder.tvAvailability.setTextColor(Color.parseColor("#F57C00"));
            holder.btnBook.setEnabled(true);
            holder.btnBook.setAlpha(1.0f);
        } else {
            holder.tvAvailability.setTextColor(Color.parseColor("#D32F2F"));
            holder.btnBook.setEnabled(false);
            holder.btnBook.setAlpha(0.5f);
        }

        holder.btnBook.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), BookingActivity.class);
            intent.putExtra("TRAIN_NUM", train.trainNumber);
            intent.putExtra("TRAIN_NAME", train.trainName);
            intent.putExtra("PRICE", train.price);
            intent.putExtra("DATE", date);
            intent.putExtra("SOURCE", source);
            intent.putExtra("DEST", dest);
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