package com.tanmay.Train_ticket_Reservation_System;

import android.content.Context;
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
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

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
        }

        holder.btnDownloadTicket.setOnClickListener(v -> generatePdf(v.getContext(), ticket));
    }

    @Override
    public int getItemCount() {
        return ticketList.size();
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
        canvas.drawText("RAILCONNECT E-TICKET", 70, 50, paint);

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