package com.tanmay.Train_ticket_Reservation_System;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // Paste your Cloudflare URL here!
    private static final String BASE_URL = "https://status-tracks-session-functions.trycloudflare.com/";
    private static Retrofit retrofit = null;

    public static TrainApi getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(TrainApi.class);
    }
}