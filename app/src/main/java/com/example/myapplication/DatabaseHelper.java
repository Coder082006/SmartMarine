package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

// This class manages our whole local database using SQLite.
// It stores three things:
//   1. users    -> people who registered / can log in
//   2. boats     -> the boats/ferries and their schedules (used by Search)
//   3. bookings  -> tickets that a user has booked (used by My Bookings)
public class DatabaseHelper extends SQLiteOpenHelper {

    // The name of our database file
    private static final String DATABASE_NAME = "SmartMarine.db";

    // Version number. We bumped this to 2 when we added the
    // boats and bookings tables so existing installs get upgraded.
    private static final int DATABASE_VERSION = 2;

    // ---- Table + column names ----
    private static final String TABLE_USERS = "users";
    private static final String TABLE_BOATS = "boats";
    private static final String TABLE_BOOKINGS = "bookings";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // This runs ONCE when the database file is first created.
    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);
        createBoatsTable(db);
        createBookingsTable(db);
        seedBoats(db);
    }

    // This runs when DATABASE_VERSION changes (e.g. 1 -> 2).
    // We only ADD the new tables so existing users are NOT lost.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        createBoatsTable(db);
        createBookingsTable(db);
        seedBoats(db);
    }

    // -------------------------------------------------------
    // TABLE CREATION
    // -------------------------------------------------------
    private void createUsersTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "full_name TEXT, "
                + "email TEXT UNIQUE, "
                + "phone TEXT, "
                + "password TEXT)";
        db.execSQL(sql);
    }

    private void createBoatsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_BOATS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT, "
                + "origin TEXT, "
                + "destination TEXT, "
                + "departure_time TEXT, "
                + "arrival_time TEXT, "
                + "price INTEGER)";
        db.execSQL(sql);
    }

    private void createBookingsTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_BOOKINGS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "reference TEXT, "
                + "user_email TEXT, "
                + "passenger_name TEXT, "
                + "boat_name TEXT, "
                + "origin TEXT, "
                + "destination TEXT, "
                + "travel_date TEXT, "
                + "departure_time TEXT, "
                + "price INTEGER, "
                + "status TEXT, "
                + "created_at INTEGER)";
        db.execSQL(sql);
    }

    // Pre-load some boats/ferries so the Search screen has real data.
    // The admin side (see proposal) would normally manage these.
    private void seedBoats(SQLiteDatabase db) {
        // Don't seed twice.
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BOATS, null);
        int count = 0;
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        if (count > 0) {
            return;
        }

        // origin, destination, name, dep, arr, price (TZS)
        insertBoat(db, "Dar es Salaam", "Zanzibar", "Azam Marine Ferry", "08:00 AM", "10:00 AM", 35000);
        insertBoat(db, "Dar es Salaam", "Zanzibar", "Kilimanjaro Fast Ferry", "10:30 AM", "12:00 PM", 40000);
        insertBoat(db, "Dar es Salaam", "Zanzibar", "Sea Star Express", "02:00 PM", "04:00 PM", 32000);

        insertBoat(db, "Zanzibar", "Dar es Salaam", "Azam Marine Ferry", "09:00 AM", "11:00 AM", 35000);
        insertBoat(db, "Zanzibar", "Dar es Salaam", "Kilimanjaro Fast Ferry", "03:30 PM", "05:00 PM", 40000);

        insertBoat(db, "Dar es Salaam", "Mafia", "Sea Star Express", "07:30 AM", "10:30 AM", 45000);
        insertBoat(db, "Mafia", "Dar es Salaam", "Sea Star Express", "01:00 PM", "04:00 PM", 45000);

        insertBoat(db, "Zanzibar", "Pemba", "Azam Marine Ferry", "11:00 AM", "01:30 PM", 30000);
        insertBoat(db, "Pemba", "Zanzibar", "Azam Marine Ferry", "02:30 PM", "05:00 PM", 30000);

        insertBoat(db, "Mwanza", "Bukoba", "Victoria Lake Ferry", "09:00 AM", "01:00 PM", 28000);
        insertBoat(db, "Bukoba", "Mwanza", "Victoria Lake Ferry", "08:00 PM", "12:00 AM", 28000);
    }

    private void insertBoat(SQLiteDatabase db, String origin, String destination,
                            String name, String dep, String arr, int price) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("origin", origin);
        v.put("destination", destination);
        v.put("departure_time", dep);
        v.put("arrival_time", arr);
        v.put("price", price);
        db.insert(TABLE_BOATS, null, v);
    }

    // -------------------------------------------------------
    // USERS — register / login / lookup
    // -------------------------------------------------------
    public boolean registerUser(String name, String email,
                                String phone, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("full_name", name);
        values.put("email", email);
        values.put("phone", phone);
        values.put("password", password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Email is matched case-insensitively (and trimmed) so "A@x.com"
        // and "a@x.com" are treated as the same account. The password is
        // still matched exactly.
        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_USERS
                        + " WHERE LOWER(TRIM(email)) = LOWER(TRIM(?)) AND password = ?",
                new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Returns true if an account with this email exists (ignoring case).
    // Lets the login screen tell the user whether it's the email or the
    // password that's wrong.
    public boolean emailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_USERS
                        + " WHERE LOWER(TRIM(email)) = LOWER(TRIM(?))",
                new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    // Returns the full name for a given email, or "" if not found.
    // Used so the ticket shows the REAL passenger instead of "John Doe".
    public String getUserName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT full_name FROM " + TABLE_USERS + " WHERE email = ?",
                new String[]{email});
        String name = "";
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return name;
    }

    // -------------------------------------------------------
    // BOATS — search available boats for a route
    // -------------------------------------------------------
    public List<Boat> searchBoats(String origin, String destination) {
        List<Boat> boats = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Case-insensitive, trimmed match on origin + destination.
        Cursor c = db.rawQuery(
                "SELECT id, name, origin, destination, departure_time, arrival_time, price "
                        + "FROM " + TABLE_BOATS + " "
                        + "WHERE LOWER(TRIM(origin)) = LOWER(TRIM(?)) "
                        + "AND LOWER(TRIM(destination)) = LOWER(TRIM(?)) "
                        + "ORDER BY departure_time",
                new String[]{origin, destination});
        while (c.moveToNext()) {
            Boat b = new Boat();
            b.id = c.getInt(0);
            b.name = c.getString(1);
            b.origin = c.getString(2);
            b.destination = c.getString(3);
            b.departureTime = c.getString(4);
            b.arrivalTime = c.getString(5);
            b.price = c.getInt(6);
            boats.add(b);
        }
        c.close();
        db.close();
        return boats;
    }

    // -------------------------------------------------------
    // BOOKINGS — create a ticket, list a user's tickets
    // -------------------------------------------------------
    // Saves a booking and returns the generated reference (e.g. SMB-2026-1005).
    public String createBooking(String userEmail, String passengerName,
                                String boatName, String origin, String destination,
                                String travelDate, String departureTime, int price) {
        SQLiteDatabase db = this.getWritableDatabase();

        String reference = generateReference(db);

        ContentValues v = new ContentValues();
        v.put("reference", reference);
        v.put("user_email", userEmail);
        v.put("passenger_name", passengerName);
        v.put("boat_name", boatName);
        v.put("origin", origin);
        v.put("destination", destination);
        v.put("travel_date", travelDate);
        v.put("departure_time", departureTime);
        v.put("price", price);
        v.put("status", "CONFIRMED");
        v.put("created_at", System.currentTimeMillis());

        long id = db.insert(TABLE_BOOKINGS, null, v);
        db.close();

        return id != -1 ? reference : null;
    }

    // Builds a reference like SMB-<year>-<number> based on how many
    // bookings already exist, so each ticket has a unique code.
    private String generateReference(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BOOKINGS, null);
        int count = 0;
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        return "SMB-" + year + "-" + (1001 + count);
    }

    // Returns all bookings for a user, newest first (for My Bookings).
    public List<Booking> getBookingsForUser(String userEmail) {
        List<Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT reference, passenger_name, boat_name, origin, destination, "
                        + "travel_date, departure_time, price, status "
                        + "FROM " + TABLE_BOOKINGS + " "
                        + "WHERE user_email = ? ORDER BY created_at DESC",
                new String[]{userEmail});
        while (c.moveToNext()) {
            Booking b = new Booking();
            b.reference = c.getString(0);
            b.passengerName = c.getString(1);
            b.boatName = c.getString(2);
            b.origin = c.getString(3);
            b.destination = c.getString(4);
            b.travelDate = c.getString(5);
            b.departureTime = c.getString(6);
            b.price = c.getInt(7);
            b.status = c.getString(8);
            bookings.add(b);
        }
        c.close();
        db.close();
        return bookings;
    }
}