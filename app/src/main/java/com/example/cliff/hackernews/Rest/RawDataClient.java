package com.example.cliff.hackernews.Rest;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

// A Retrofit client built to get raw data

public class RawDataClient {

    // This URL will be overridden because of @Url in EndPointHTML
    public static final String BASE_URL = "https://hacker-news.firebaseio.com/v0/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit==null) {

            // Interceptors allow us to intercept the raw data with a client
                // Required interceptor dependency
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor).build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // Ignored, but necessary to define
                    .client(client) // Add the client with interceptor to retrofit
                    .build();
            // No converter needed, since we want the raw HTML String
        }
        return retrofit;
    }
}
