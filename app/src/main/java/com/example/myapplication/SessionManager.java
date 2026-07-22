package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

// Keeps track of WHICH user is currently logged in.
// This is only a lightweight session key (the user's email) — all the
// real data (users, boats, bookings) lives in the SQLite database.
// We look the full user record up from SQLite using this email.
public class SessionManager {

    private static final String PREF_NAME = "smart_marine_session";
    private static final String KEY_EMAIL = "logged_in_email";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Call this right after a successful login.
    public void login(String email) {
        prefs.edit().putString(KEY_EMAIL, email).apply();
    }

    // Call this on logout to clear the session.
    public void logout() {
        prefs.edit().remove(KEY_EMAIL).apply();
    }

    // Returns the logged-in user's email, or "" if nobody is logged in.
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public boolean isLoggedIn() {
        return !getEmail().isEmpty();
    }
}