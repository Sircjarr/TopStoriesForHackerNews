package com.example.cliff.hackernews;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Cliff on 7/6/2017.
 */

public class DBHandler extends SQLiteOpenHelper {
    // Adding columns to the database will affect the version
    private static final int DATABASE_VERSION = 2;
    // Name of file to store data
    private static final String DATABASE_NAME = "hackernews.db";
    public static final String TABLE_HACKERNEWS = "hackernews";
    // Columns in the table
    private static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLES = "titles";
    public static final String COLUMN_CONTENT = "content";

    public DBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    //  Run only when the database file did not exist and was just created
    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_HACKERNEWS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TITLES + " TEXT, " +
                COLUMN_CONTENT + " TEXT);";
        db.execSQL(query);
    }

    // Called when version is upgraded; clears the table and creates a new one
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // DROP = delete entire
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HACKERNEWS);
        onCreate(db);
    }

    public void addArticle(String title, String content) {
        // Set different values for different columns and insert with one statement
            // ContentValues is basically a list of values
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLES, title);
        values.put(COLUMN_CONTENT, content);

        // Set db = database to write to
        SQLiteDatabase db = getWritableDatabase();

        // insert a new row into table
        db.insert(TABLE_HACKERNEWS, null, values);
    }

    public void clearDatabase() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_HACKERNEWS);
    }
}
