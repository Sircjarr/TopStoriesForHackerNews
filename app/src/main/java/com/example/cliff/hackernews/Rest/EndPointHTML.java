package com.example.cliff.hackernews.Rest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

// End point method for getting raw HTML data from a specified url

public interface EndPointHTML {

    // Get the data at the url
    @GET
    Call<ResponseBody> getHTML(@Url String url); // ignore the BASE_URL

}

