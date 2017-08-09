package com.example.cliff.hackernews;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final int SQLITE = 0;
    public static final int ASYNCTASK = 1;
    public static final int RETROFIT = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v) {
        final int id = v.getId();
        int command = 0;
        switch (id) {
            case R.id.btnReadFromSQL:
                command = SQLITE;
                break;
            case R.id.btnAsyncTask:
                command = ASYNCTASK;
                break;
            case R.id.btnRetrofit:
                command = RETROFIT;
                break;
        }

        Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
        intent.putExtra("command", command);
        startActivity(intent);
    }
}
