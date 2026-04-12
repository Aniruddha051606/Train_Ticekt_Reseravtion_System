package com.tanmay.Train_ticket_Reservation_System;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private final List<TrainApi.Ticket> ticketList;

    public TicketAdapter(List<TrainApi.Ticket> ticketList) {
        this.ticketList = ticketList;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        TrainApi.Ticket ticket = ticketList.get(position);

        holder.tvTicketPnr.setText("PNR: " + ticket.pnr);
        holder.tvTicketStatus.setText(ticket.status);
        holder.tvTicketTrain.setText(ticket.trainNumber + " - " + ticket.trainName);
        holder.tvTicketRoute.setText(ticket.source + " ➔ " + ticket.destination + " | " + ticket.date);
        holder.tvTicketPrice.setText("₹ " + ticket.totalPrice);

        // 🌟 FAANG UPDATE: Handle specific colors for all statuses
        if (ticket.status.equalsIgnoreCase("WAITLIST")) {
            holder.tvTicketStatus.setTextColor(Color.parseColor("#F57C00")); // Orange
        } else if (ticket.status.equalsIgnoreCase("CANCELLED")) {
            holder.tvTicketStatus.setTextColor(Color.parseColor("#D32F2F")); // Red
        } else {
            holder.tvTicketStatus.setTextColor(Color.parseColor("#388E3C")); // Green (Confirmed)
        }

        // Keep PDF Download
        holder.btnDownloadTicket.setOnClickListener(v -> generatePdf(v.getContext(), ticket));

        // 🌟 FAANG UPDATE: Cancel Ticket via Long Click
        holder.itemView.setOnLongClickListener(v -> {
            if (ticket.status.equalsIgnoreCase("CANCELLED")) {
                Toast.makeText(v.getContext(), "Ticket is already cancelled.", Toast.LENGTH_SHORT).show();
                return true;
            }

            new AlertDialog.Builder(v.getContext())
                    .setTitle("Cancel Ticket?")
                    .setMessage("Are you sure you want to cancel PNR " + ticket.pnr + "? You will receive an 80% refund.")
                    .setPositiveButton("Yes, Cancel", (dialog, which) -> cancelTicketApi(v.getContext(), ticket.pnr, position))
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
    }

    // 🌟 FAANG UPDATE: The API call to Node.js Cancellation Engine
    private void cancelTicketApi(Context context, String pnr, int position) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        TrainApi api = RetrofitClient.getApiService();
        api.cancelTicket("Bearer " + token, pnr).enqueue(new Callback<TrainApi.CancelResponse>() {
            @Override
            public void onResponse(Call<TrainApi.CancelResponse> call, Response<TrainApi.CancelResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(context, "✅ Ticket Cancelled! Refund: ₹" + response.body().refundAmount, Toast.LENGTH_LONG).show();
                    // Update UI locally to show CANCELLED
                    ticketList.get(position).status = "CANCELLED";
                    notifyItemChanged(position);
                } else {
                    Toast.makeText(context, "❌ Cancellation Failed.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TrainApi.CancelResponse> call, Throwable t) {
                Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generatePdf(Context context, TrainApi.Ticket ticket) {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(400, 600, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        paint.setTextSize(24f);
        paint.setFakeBoldText(true);
        paint.setColor(Color.parseColor("#0A1628"));
        canvas.drawText("Train Ticket Reservation System E-TICKET", 50, 50, paint);

        paint.setTextSize(16f);
        paint.setFakeBoldText(false);
        paint.setColor(Color.BLACK);
        canvas.drawText("PNR Number: " + ticket.pnr, 40, 100, paint);
        canvas.drawText("Status: " + ticket.status, 40, 130, paint);
        canvas.drawText("Train: " + ticket.trainNumber + " - " + ticket.trainName, 40, 170, paint);
        canvas.drawText("Route: " + ticket.source + " to " + ticket.destination, 40, 200, paint);
        canvas.drawText("Date: " + ticket.date, 40, 230, paint);
        canvas.drawText("Total Fare: ₹" + ticket.totalPrice, 40, 260, paint);

        pdfDocument.finishPage(page);

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Ticket_" + ticket.pnr + ".pdf");
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(context, "✅ Ticket Saved to Downloads!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(context, "❌ Failed to download", Toast.LENGTH_SHORT).show();
        }
        pdfDocument.close();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        TextView tvTicketPnr, tvTicketStatus, tvTicketTrain, tvTicketRoute, tvTicketPrice;
        Button btnDownloadTicket;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTicketPnr = itemView.findViewById(R.id.tvTicketPnr);
            tvTicketStatus = itemView.findViewById(R.id.tvTicketStatus);
            tvTicketTrain = itemView.findViewById(R.id.tvTicketTrain);
            tvTicketRoute = itemView.findViewById(R.id.tvTicketRoute);
            tvTicketPrice = itemView.findViewById(R.id.tvTicketPrice);
            btnDownloadTicket = itemView.findViewById(R.id.btnDownloadTicket);
        }
    }
}