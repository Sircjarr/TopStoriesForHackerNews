package com.example.cliff.hackernews.Model;

import com.google.gson.annotations.SerializedName;

// POJO class. Aka "Plain Old Java Object"
// This class models the JSON architecture at the API call

public class Story {

    @SerializedName("title")
    private String title;

    @SerializedName("url")
    private String url;

    public Story(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }
}
