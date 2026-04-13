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

        holder.tvTrainName.setText(train.trainName);
        holder.tvTrainNumber.setText("#" + train.trainNumber);
        holder.tvDepartureTime.setText(train.departureTime);
        holder.tvArrivalTime.setText(train.arrivalTime);
        holder.tvSource.setText(source != null ? source.toUpperCase() : "");
        holder.tvDestination.setText(dest != null ? dest.toUpperCase() : "");
        holder.tvPrice.setText("₹" + train.price);
        holder.tvDuration.setText(train.availability);

        if (train.availability != null && train.availability.startsWith("AVL")) {
            holder.tvDuration.setBackgroundColor(Color.parseColor("#E8F5E9"));
            holder.tvDuration.setTextColor(Color.parseColor("#2E7D32"));
            holder.btnBook.setEnabled(true);
        } else {
            holder.tvDuration.setBackgroundColor(Color.parseColor("#FFF3E0"));
            holder.tvDuration.setTextColor(Color.parseColor("#E65100"));
        }

        // Route to your existing BookingActivity!
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
        TextView tvTrainName, tvTrainNumber, tvDuration, tvDepartureTime, tvSource, tvArrivalTime, tvDestination, tvPrice;
        Button btnBook;

        public TrainViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTrainName = itemView.findViewById(R.id.tvTrainName);
            tvTrainNumber = itemView.findViewById(R.id.tvTrainNumber);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvDepartureTime = itemView.findViewById(R.id.tvDepartureTime);
            tvSource = itemView.findViewById(R.id.tvSource);
            tvArrivalTime = itemView.findViewById(R.id.tvArrivalTime);
            tvDestination = itemView.findViewById(R.id.tvDestination);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnBook = itemView.findViewById(R.id.btnBook);
        }
    }
}