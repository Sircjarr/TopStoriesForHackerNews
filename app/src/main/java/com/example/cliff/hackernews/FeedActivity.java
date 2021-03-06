package com.example.cliff.hackernews;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cliff.hackernews.Model.Story;
import com.example.cliff.hackernews.Rest.APIClient;
import com.example.cliff.hackernews.Rest.EndPointHTML;
import com.example.cliff.hackernews.Rest.EndPoints;
import com.example.cliff.hackernews.Rest.RawDataClient;
import com.example.cliff.hackernews.Util.DBHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedActivity extends AppCompatActivity {

    private static final String TAG = "FeedActivity";

    // Widgets
    private ProgressDialog progressDialog;
    private TextView tvTaskSpeed;
    private ListView lvTitles;

    // ListView stuff
    public static int listViewSize = 15;
    private ArrayList<String> titles = new ArrayList<>();
    private ArrayList<String> content = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;

    // Command Strings passed in from Main_Activity
    private final String COMMAND_SQLITE = "Retrieving from SQLite Database";
    private final String COMMAND_ASYNCTASK = "Refreshing with AsyncTask";
    private final String COMMAND_RETROFIT = "Refreshing with Retrofit";
    private final String NO_SQL_DATA = "No data in SQLite Database";

    // Detect which command was given
    private int command;

    // Execution speed variables
    private String speedOfTask;
    private double taskBegin;
    private double taskEnd;

    // Database Helper class
    DBHandler dbHandler;

    // For Retrofit to access in AsyncTask
    private int[] arrStoryID;

    // Proper way to setup our RF services
    private final EndPoints apiService = APIClient.getClient().create(EndPoints.class);
    private final EndPointHTML rawDataService = RawDataClient.getClient().create(EndPointHTML.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        // Create or load SQLiteDatabase with SQLiteOpenHelper
        dbHandler = new DBHandler(this, null, null, 1);

        // Create the ProgressDialog
        progressDialog = new ProgressDialog(FeedActivity.this);

        // Get the command from MA
        Intent intent = getIntent();
        command = intent.getIntExtra("command", 0);

        // Update the listView with the command
        switch(command) {
            case MainActivity.SQLITE:

                progressDialog.setMessage(COMMAND_SQLITE);
                progressDialog.show();

                speedOfTask = COMMAND_SQLITE;

                readFromSQL();
                break;

            case MainActivity.ASYNCTASK:

                progressDialog.setMessage(COMMAND_ASYNCTASK);
                progressDialog.show();

                speedOfTask = COMMAND_ASYNCTASK;

                taskBegin = System.currentTimeMillis();
                refreshWithAsyncTask();
                break;

            case MainActivity.RETROFIT:

                progressDialog.setMessage(COMMAND_RETROFIT);
                progressDialog.show();

                speedOfTask = COMMAND_RETROFIT;

                taskBegin = System.currentTimeMillis();
                refreshWithRetrofit();
                break;
        }
    }

    // --------------------- SQLite Database ------------------------------------------
    public void readFromSQL() {
        Cursor c = dbHandler.getCursor();

        int titleIndex = c.getColumnIndex(DBHandler.COLUMN_TITLES);
        int contentIndex = c.getColumnIndex(DBHandler.COLUMN_CONTENT);

        if (c.moveToFirst()) {
            // Database has items

            do {
                titles.add(c.getString(titleIndex));
                content.add(c.getString(contentIndex));

            } while (c.moveToNext());

            setTitleListView();
            c.close();
        }
    }

    // ------------------ AsyncTask--------------------------
    public void refreshWithAsyncTask() {
        // Begin extracting data from Hacker News
        DownloadTask task = new DownloadTask();
        try {
            task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load Hacker News", Toast.LENGTH_LONG).show();
        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            // Get the ID's of the articles
            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                // Establish how big ListView should be
                JSONArray ja = new JSONArray(sb.toString());
                if (ja.length() < listViewSize) {
                    listViewSize = ja.length();
                }

                // Clear Database
                dbHandler.clearDatabase();

                // Get title and html content of url
                int count = 0;
                for (int i = 0; count < listViewSize; i++) {

                    // Access article data with the ID
                    url = new URL("https://hacker-news.firebaseio.com/v0/item/" +
                            ja.getString(i) + ".json?print=pretty");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    sb.setLength(0);
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }

                    JSONObject jo = new JSONObject(sb.toString());

                    // Store titles and content of URLS if they exist
                    if (!jo.isNull("title") && !jo.isNull("url")) {

                        url = new URL(jo.getString("url"));
                        urlConnection = (HttpURLConnection) url.openConnection();
                        reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                        sb.setLength(0);
                        while ((line = reader.readLine()) != null) {
                            sb.append(line).append("\n");
                        }

                        dbHandler.addArticle(jo.getString("title"), sb.toString());
                        count++;
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            taskEnd = System.currentTimeMillis();
            readFromSQL();
        }
    }

    // --------------------- Retrofit ------------------------------
    public void refreshWithRetrofit() {

        // Create the ID call with the right service
        Call<int[]> call = apiService.getID();

        // Asynchronous call
        call.enqueue(new Callback<int[]>() {
            @Override
            public void onResponse(@NonNull Call<int[]> call, @NonNull Response<int[]> response) {

                // Establish how big ListView should be
                if (response.body().length < listViewSize) {
                    listViewSize = response.body().length;
                }
                arrStoryID = response.body();

                // Use AsyncTask to make synchronous RF calls
                RetroAsyncTask retroAsyncTask = new RetroAsyncTask();
                retroAsyncTask.execute();
            }

            @Override
            public void onFailure(@NonNull Call<int[]> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure: ");
            }
        });

    }

    public class RetroAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {

            // Clear database
            dbHandler.clearDatabase();

            // Get titles and content from url
            int count = 0;
            for (int i = 0; count < listViewSize; i++) {

                try {

                    // Construct the storyCall with the right service
                    final Call<Story> storyCall = apiService.getStory(arrStoryID[i]);

                    // RF synchronous call
                        // Allowed only on background threads
                    Response<Story> storyResponse = storyCall.execute();

                    if (storyResponse.body().getTitle() != null && storyResponse.body().getUrl() != null) {

                        // Get raw html with Retrofit
                        final Call<ResponseBody> htmlCall = rawDataService.getHTML(storyResponse.body().getUrl());
                        Response<ResponseBody> htmlResponse = htmlCall.execute();

                        // Add the title and html Data to the SQLite Database
                        dbHandler.addArticle(storyResponse.body().getTitle(), htmlResponse.body().string());

                        count++;
                    }
                }
                catch(IOException e) {
                    Log.d(TAG, "doInBackground: " + e.getMessage());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            taskEnd = System.currentTimeMillis();
            readFromSQL();
        }
    }

    // -------------- Setup ListView ------------------------
    public void setTitleListView() {

        tvTaskSpeed = (TextView) findViewById(R.id.tvTaskSpeed);
        lvTitles = (ListView) findViewById(R.id.lvTitles);

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        lvTitles.setAdapter(arrayAdapter);

        lvTitles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), WebActivity.class);
                intent.putExtra("content", content.get(position));
                startActivity(intent);
            }
        });

        progressDialog.hide();

        // Display the speed of the requested task
        if (command != MainActivity.SQLITE) {

            if (titles.size() != 0) {

                double taskSpeed = ((taskEnd - taskBegin) / 1000);
                taskSpeed = round(taskSpeed, 1);

                speedOfTask += " took\n" + taskSpeed + " seconds";
                tvTaskSpeed.setText(speedOfTask);

            } else {

                tvTaskSpeed.setText(NO_SQL_DATA);

            }
        }
        else {
            tvTaskSpeed.setText("Retrieved from SQLite Database");
        }
    }

    private static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }
}

