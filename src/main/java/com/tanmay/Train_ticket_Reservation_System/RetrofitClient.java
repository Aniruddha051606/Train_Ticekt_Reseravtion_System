package com.tanmay.Train_ticket_Reservation_System;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://train-ticket-reservation-system-ftnk.onrender.com/";
    private static Retrofit retrofit = null;

    public static TrainApi getApiService() {
        if (retrofit == null) {

            // 🌟 NEW: Tell Android to wait 60 seconds for Render to wake up!
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient) // <-- Attach the custom client here
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(TrainApi.class);
    }
}