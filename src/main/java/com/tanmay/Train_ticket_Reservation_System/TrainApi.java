package com.tanmay.Train_ticket_Reservation_System;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface TrainApi {

    @GET("/searchStation")
    Call<List<Station>> searchStations(@Query("searchQuery") String query);

    @GET("/searchTrains")
    Call<List<Train>> getTrains(
            @Query("source") String source,
            @Query("destination") String destination,
            @Query("date") String date
    );

    @POST("/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("/auth/google")
    Call<AuthResponse> googleLogin(@Body GoogleLoginRequest request);

    @POST("/bookTicket")
    Call<BookingResponse> bookTicket(@Header("Authorization") String token, @Body BookTicketRequest request);

    // 🌟 THE MISSING GET TICKETS ROUTE
    @GET("/myTickets")
    Call<List<Ticket>> getMyTickets(@Header("Authorization") String token);


    // ==========================================
    // DTO CLASSES (Data Models)
    // ==========================================

    class Station {
        public String name;
        public String code;
        @Override public String toString() { return name + " - " + code; }
    }

    class Train {
        public String trainNumber;
        public String trainName;
        public String departureTime;
        public String arrivalTime;
        public String availability;
        public int price;
    }

    class LoginRequest {
        public String email;
        public String password;
        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    class RegisterRequest {
        public String name;
        public String email;
        public String password;
        public RegisterRequest(String name, String email, String password) {
            this.name = name;
            this.email = email;
            this.password = password;
        }
    }

    class GoogleLoginRequest {
        public String idToken;
        public GoogleLoginRequest(String idToken) {
            this.idToken = idToken;
        }
    }

    class AuthResponse {
        public String token;
        public UserData user;
        public String message;
        public String error;
    }

    class UserData {
        public String name;
        public String email;
    }

    class BookTicketRequest {
        public String trainNumber;
        public String trainName;
        public String source;
        public String destination;
        public String date;
        public List<Passenger> passengers;
        public int totalPrice;
    }

    class Passenger {
        public String name;
        public int age;
        public String gender;
    }

    class BookingResponse {
        public String message;
        public String pnr;
        public String status;
    }

    // 🌟 THE MISSING TICKET MODEL
    class Ticket {
        public String trainNumber;
        public String trainName;
        public String source;
        public String destination;
        public String date;
        public String pnr;
        public String status;
        public int totalPrice;
    }
}