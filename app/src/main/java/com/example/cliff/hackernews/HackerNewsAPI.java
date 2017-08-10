package com.example.cliff.hackernews;


import retrofit2.http.GET;
import retrofit2.http.Headers;

public interface HackerNewsAPI {

    public static final String BASE_URL = "https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty";
    // String STORY_URL ="https://hacker-news.firebaseio.com/v0/item/14980512.json?print=pretty";

   //@Headers("Content-Type: application/json")
    //@GET(".json")
    //Call<Feed> getData();

}
