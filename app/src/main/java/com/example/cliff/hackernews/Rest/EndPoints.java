package com.example.cliff.hackernews.Rest;

import com.example.cliff.hackernews.Model.Story;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

// End point methods for getting Story ids, and the Story data

public interface EndPoints {

    // Static call
        // GET requests will be prefixed with the common BASE_URL
    @GET("topstories.json?print=pretty")
    Call<int[]> getID();

    // Dynamic call
        // Where {storyID} will be substituted with int storyID when getStory() method is called in Activity
    @GET("item/{storyID}.json?print=pretty")
    Call<Story> getStory(@Path("storyID") int storyID);

}