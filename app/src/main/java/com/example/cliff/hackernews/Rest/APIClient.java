package com.example.cliff.hackernews.Rest;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// This is where we retrieve our custom Retrofit client

public class APIClient {

    public static final String BASE_URL = "https://hacker-news.firebaseio.com/v0/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit==null) {

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    // Converters allow us to convert our String data into a readable format
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
