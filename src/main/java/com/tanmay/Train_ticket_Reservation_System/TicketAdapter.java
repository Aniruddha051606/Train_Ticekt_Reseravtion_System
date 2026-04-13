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

        if (ticket.status.equalsIgnoreCase("WAITLIST")) {
            holder.tvTicketStatus.setTextColor(Color.parseColor("#F57C00"));
        } else if (ticket.status.equalsIgnoreCase("CANCELLED")) {
            holder.tvTicketStatus.setTextColor(Color.parseColor("#D32F2F"));
        } else {
            holder.tvTicketStatus.setTextColor(Color.parseColor("#388E3C"));
        }

        // Trigger the upgraded Premium PDF Generator
        holder.btnDownloadTicket.setOnClickListener(v -> generatePremiumPdf(v.getContext(), ticket));

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

    private void cancelTicketApi(Context context, String pnr, int position) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", "");

        TrainApi api = RetrofitClient.getApiService();
        api.cancelTicket("Bearer " + token, pnr).enqueue(new Callback<TrainApi.CancelResponse>() {
            @Override
            public void onResponse(Call<TrainApi.CancelResponse> call, Response<TrainApi.CancelResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(context, "✅ Ticket Cancelled! Refund: ₹" + response.body().refundAmount, Toast.LENGTH_LONG).show();
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

    // 🌟 UPGRADED: Premium PDF Drawing Logic
    private void generatePremiumPdf(Context context, TrainApi.Ticket ticket) {
        PdfDocument pdfDocument = new PdfDocument();
        // Modern ticket aspect ratio
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(600, 800, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // 1. Draw App Background (Kinetic Surface Color)
        paint.setColor(Color.parseColor("#FFF8F6"));
        canvas.drawRect(0, 0, 600, 800, paint);

        // 2. Draw Top Header Bar (Blue)
        paint.setColor(Color.parseColor("#002068"));
        canvas.drawRect(0, 0, 600, 120, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(36f);
        paint.setFakeBoldText(true);
        canvas.drawText("Train Ticket Reservation System", 40, 75, paint);

        paint.setTextSize(16f);
        paint.setFakeBoldText(false);
        paint.setColor(Color.parseColor("#FEA189")); // Peach accent
        canvas.drawText("PREMIUM E-TICKET", 410, 70, paint);

        // 3. Draw Main White Ticket Card
        paint.setColor(Color.WHITE);
        canvas.drawRoundRect(40, 160, 560, 700, 24f, 24f, paint);

        // 4. Draw Ticket Content Inside Card
        paint.setColor(Color.parseColor("#888888"));
        paint.setTextSize(14f);
        canvas.drawText("PNR NUMBER", 80, 210, paint);

        paint.setColor(Color.parseColor("#111111"));
        paint.setTextSize(28f);
        paint.setFakeBoldText(true);
        canvas.drawText(ticket.pnr != null ? ticket.pnr : "N/A", 80, 250, paint);

        // Dynamic Status Color
        paint.setTextSize(16f);
        paint.setFakeBoldText(true);
        if (ticket.status.equalsIgnoreCase("CONFIRMED")) {
            paint.setColor(Color.parseColor("#2E7D32"));
        } else if (ticket.status.equalsIgnoreCase("WAITLIST")) {
            paint.setColor(Color.parseColor("#F57C00"));
        } else {
            paint.setColor(Color.parseColor("#D32F2F"));
        }
        canvas.drawText(ticket.status.toUpperCase(), 420, 245, paint);

        // Top Divider Line
        paint.setColor(Color.parseColor("#EEEEEE"));
        paint.setStrokeWidth(2f);
        canvas.drawLine(80, 290, 520, 290, paint);

        // Route: Source ➔ Destination
        paint.setColor(Color.parseColor("#888888"));
        paint.setTextSize(14f);
        paint.setFakeBoldText(false);
        canvas.drawText("FROM", 80, 340, paint);
        canvas.drawText("TO", 460, 340, paint);

        paint.setColor(Color.parseColor("#701600")); // Kinetic Red
        paint.setTextSize(36f);
        paint.setFakeBoldText(true);
        canvas.drawText(ticket.source != null ? ticket.source : "--", 80, 385, paint);
        canvas.drawText(ticket.destination != null ? ticket.destination : "--", 460, 385, paint);

        paint.setColor(Color.parseColor("#CCCCCC"));
        paint.setTextSize(24f);
        canvas.drawText("➔", 280, 380, paint);

        // Date & Train Number
        paint.setColor(Color.parseColor("#888888"));
        paint.setTextSize(14f);
        paint.setFakeBoldText(false);
        canvas.drawText("DATE OF JOURNEY", 80, 460, paint);
        canvas.drawText("TRAIN", 80, 540, paint);

        paint.setColor(Color.parseColor("#111111"));
        paint.setTextSize(20f);
        paint.setFakeBoldText(true);
        canvas.drawText(ticket.date != null ? ticket.date : "--", 80, 490, paint);
        canvas.drawText(ticket.trainNumber + " - " + ticket.trainName, 80, 570, paint);

        // Bottom Divider Line
        paint.setColor(Color.parseColor("#EEEEEE"));
        canvas.drawLine(40, 620, 560, 620, paint);

        // Total Fare Footer
        paint.setColor(Color.parseColor("#888888"));
        paint.setTextSize(16f);
        paint.setFakeBoldText(false);
        canvas.drawText("TOTAL FARE", 80, 665, paint);

        paint.setColor(Color.parseColor("#002068"));
        paint.setTextSize(32f);
        paint.setFakeBoldText(true);
        canvas.drawText("₹ " + ticket.totalPrice, 380, 675, paint);

        // Ticket Footer Message
        paint.setColor(Color.parseColor("#888888"));
        paint.setTextSize(12f);
        paint.setFakeBoldText(false);
        canvas.drawText("Wish you a happy and safe journey! • Indian Railways", 150, 750, paint);

        pdfDocument.finishPage(page);

        // Save PDF to Downloads Folder
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Ticket_" + ticket.pnr + ".pdf");
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(context, "✅ Premium Ticket Saved to Downloads!", Toast.LENGTH_LONG).show();
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