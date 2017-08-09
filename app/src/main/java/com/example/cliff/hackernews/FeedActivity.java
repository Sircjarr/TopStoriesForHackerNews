package com.example.cliff.hackernews;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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

public class FeedActivity extends AppCompatActivity {

    ProgressDialog progressDialog;

    DBHandler dbHandler;

    private final String COMMAND_SQLITE = "Reading from SQLite Database";
    private final String COMMAND_ASYNCTASK = "Refreshing with AsyncTask";
    private final String COMMAND_RETROFIT = "Refreshing with Retrofit";
    private final String NO_SQL_DATA = "No data in SQLite Database";

    ListView titleListView;

    ArrayList<String> titles = new ArrayList<>();
    ArrayList<String> content = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;

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
        int command = intent.getIntExtra("command", 0);

        // Update the listView with the command
        switch(command) {
            case MainActivity.SQLITE:

                progressDialog.setMessage(COMMAND_SQLITE);
                progressDialog.show();

                readFromSQL();
                break;

            case MainActivity.ASYNCTASK:

                progressDialog.setMessage(COMMAND_ASYNCTASK);
                progressDialog.show();

                refreshWithAsyncTask();
                break;

            case MainActivity.RETROFIT:

                progressDialog.setMessage(COMMAND_RETROFIT);
                progressDialog.show();

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

            do {
                titles.add(c.getString(titleIndex));
                content.add(c.getString(contentIndex));

            } while (c.moveToNext());

            setTitleListView();
            c.close();
        }
        else {
            // Set text to no Data in the database
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
            String result = "";
            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    result += line;
                }

                // Establish how big ListView should be
                int listViewSize = 15;
                JSONArray ja = new JSONArray(result);
                if (ja.length() < listViewSize) {
                    listViewSize = ja.length();
                }

                dbHandler.clearDatabase();
                int count = 0;

                for (int i = 0; count < listViewSize; i++) {

                    // Access article data with the ID
                    result = "";
                    url = new URL("https://hacker-news.firebaseio.com/v0/item/" +
                            ja.getString(i) + ".json?print=pretty");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    while ((line = reader.readLine()) != null) {
                        result += line;
                    }

                    JSONObject jo = new JSONObject(result);

                    // Store titles and content of URLS if they exist
                    if (!jo.isNull("title") && !jo.isNull("url")) {

                        result = "";
                        url = new URL(jo.getString("url"));
                        urlConnection = (HttpURLConnection) url.openConnection();
                        reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        while ((line = reader.readLine()) != null) {
                            result += (line + "\n");
                        }

                        dbHandler.addArticle(jo.getString("title"), result);
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

            readFromSQL();
        }
    }

    // --------------------- Retrofit ------------------------------
    public void refreshWithRetrofit() {
        ;
    }

    // -------------- Setup ListView ------------------------
    public void setTitleListView() {
        titleListView = (ListView) findViewById(R.id.titleListView);

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles);
        titleListView.setAdapter(arrayAdapter);

        titleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), WebActivity.class);
                intent.putExtra("content", content.get(position));
                startActivity(intent);
            }
        });

        progressDialog.hide();
    }
}

