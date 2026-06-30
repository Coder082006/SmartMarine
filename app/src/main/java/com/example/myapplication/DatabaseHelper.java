package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// This class manages our database
// It creates the database and handles saving and reading data
public class DatabaseHelper extends SQLiteOpenHelper {

    // The name of our database file
    private static final String DATABASE_NAME = "SmartMarine.db";

    // Version number - keep it as 1
    private static final int DATABASE_VERSION = 1;

    // The name of our table where users will be saved
    private static final String TABLE_USERS = "users";

    // Column names - these are like the headers of a table
    private static final String COL_ID = "id";
    private static final String COL_NAME = "full_name";
    private static final String COL_EMAIL = "email";
    private static final String COL_PHONE = "phone";
    private static final String COL_PASSWORD = "password";

    // Constructor - this runs when we create a DatabaseHelper object
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // This method runs ONCE when the database is first created
    // It creates the users table
    @Override
    public void onCreate(SQLiteDatabase db) {

        // This is the SQL command that creates our users table
        // Think of it like creating an Excel sheet with columns
        String createTable = "CREATE TABLE users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "full_name TEXT, "
                + "email TEXT UNIQUE, "
                + "phone TEXT, "
                + "password TEXT)";

        // Run the SQL command
        db.execSQL(createTable);
    }

    // This runs if we update the database version
    // It deletes the old table and creates a fresh one
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // -------------------------------------------------------
    // METHOD 1 — SAVE A NEW USER TO THE DATABASE (REGISTER)
    // -------------------------------------------------------
    public boolean registerUser(String name, String email,
                                String phone, String password) {

        // Open the database for writing
        SQLiteDatabase db = this.getWritableDatabase();

        // ContentValues is like a container that holds
        // the data we want to save
        ContentValues values = new ContentValues();
        values.put("full_name", name);
        values.put("email", email);
        values.put("phone", phone);
        values.put("password", password);

        // Insert the data into the users table
        // result will be -1 if something went wrong
        long result = db.insert("users", null, values);

        // Close the database after we are done
        db.close();

        // If result is NOT -1 then saving was successful
        // Return true means success, false means failed
        if (result != -1) {
            return true;
        } else {
            return false;
        }
    }

    // -------------------------------------------------------
    // METHOD 2 — CHECK IF USER EXISTS IN DATABASE (LOGIN)
    // -------------------------------------------------------
    public boolean loginUser(String email, String password) {

        // Open the database for reading
        SQLiteDatabase db = this.getReadableDatabase();

        // Search the users table for a row where
        // email AND password both match
        Cursor cursor = db.rawQuery(
                "SELECT * FROM users WHERE email = ? AND password = ?",
                new String[]{email, password});

        // If cursor found at least 1 row then user exists
        if (cursor.getCount() > 0) {
            // Close cursor and database
            cursor.close();
            db.close();
            // Return true means user was found
            return true;
        } else {
            // Close cursor and database
            cursor.close();
            db.close();
            // Return false means user was not found
            return false;
        }
    }
}