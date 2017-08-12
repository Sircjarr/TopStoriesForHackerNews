package com.example.cliff.hackernews.Rest;

import retrofit2.Retrofit;

// A Retrofit client built to get raw data

public class RawDataClient {

    // This URL will be overridden because of @Url in EndPointHTML
    public static final String BASE_URL = "https://hacker-news.firebaseio.com/v0/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit==null) {

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // Ignored, but necessary to define
                    .build();
            // No converter needed, since we want the raw HTML String
        }
        return retrofit;
    }
}
