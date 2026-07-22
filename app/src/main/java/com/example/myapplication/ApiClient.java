package com.example.myapplication;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// Optional cloud client for the Node.js REST API (see /server).
// The app works fully offline using the local SQLite database; this class
// lets it ALSO fetch schedules / store bookings from the hosted API once
// you deploy it and set BASE_URL below.
//
//  >>> AFTER DEPLOYING TO RENDER, PASTE YOUR URL HERE <<<
public class ApiClient {

    // e.g. "https://smart-marine-api.onrender.com"
    private static final String BASE_URL = "https://smartmarine.onrender.com";

    public interface BoatsCallback {
        void onSuccess(List<Boat> boats);
        void onError(String message);
    }

    public interface BookingsCallback {
        void onSuccess(List<Booking> bookings);
        void onError(String message);
    }

    public interface CreateBookingCallback {
        void onSuccess(String reference);
        void onError(String message);
    }

    // True only once a real Render URL has been set.
    public static boolean isConfigured() {
        return !BASE_URL.equals("YOUR_RENDER_URL") && !BASE_URL.isEmpty();
    }

    // GET /api/boats?from=&to=
    public static void searchBoats(Context context, String from, String to,
                                   BoatsCallback callback) {
        if (!isConfigured()) {
            callback.onError("REST API URL not set");
            return;
        }

        String url = BASE_URL + "/api/boats?from=" + android.net.Uri.encode(from)
                + "&to=" + android.net.Uri.encode(to);

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<Boat> boats = new ArrayList<>();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject o = response.getJSONObject(i);
                            Boat b = new Boat();
                            b.id = o.optInt("id");
                            b.name = o.optString("name");
                            b.origin = o.optString("origin");
                            b.destination = o.optString("destination");
                            b.departureTime = o.optString("departure_time");
                            b.arrivalTime = o.optString("arrival_time");
                            b.price = o.optInt("price");
                            boats.add(b);
                        }
                        callback.onSuccess(boats);
                    } catch (Exception e) {
                        callback.onError("Could not read boats data");
                    }
                },
                error -> callback.onError("Boats request failed"));

        getQueue(context).add(request);
    }

    // POST /api/bookings
    public static void createBooking(Context context, Booking booking, String userEmail,
                                     CreateBookingCallback callback) {
        if (!isConfigured()) {
            callback.onError("REST API URL not set");
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("user_email", userEmail);
            body.put("passenger_name", booking.passengerName);
            body.put("boat_name", booking.boatName);
            body.put("origin", booking.origin);
            body.put("destination", booking.destination);
            body.put("travel_date", booking.travelDate);
            body.put("departure_time", booking.departureTime);
            body.put("price", booking.price);
        } catch (Exception e) {
            callback.onError("Could not build request");
            return;
        }

        String url = BASE_URL + "/api/bookings";
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST, url, body,
                response -> callback.onSuccess(response.optString("reference")),
                error -> callback.onError("Booking request failed"));

        getQueue(context).add(request);
    }

    // GET /api/bookings?email=
    public static void getBookings(Context context, String email,
                                   BookingsCallback callback) {
        if (!isConfigured()) {
            callback.onError("REST API URL not set");
            return;
        }

        String url = BASE_URL + "/api/bookings?email=" + android.net.Uri.encode(email);
        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET, url, null,
                response -> {
                    List<Booking> bookings = new ArrayList<>();
                    try {
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject o = response.getJSONObject(i);
                            Booking b = new Booking();
                            b.reference = o.optString("reference");
                            b.passengerName = o.optString("passenger_name");
                            b.boatName = o.optString("boat_name");
                            b.origin = o.optString("origin");
                            b.destination = o.optString("destination");
                            b.travelDate = o.optString("travel_date");
                            b.departureTime = o.optString("departure_time");
                            b.price = o.optInt("price");
                            b.status = o.optString("status");
                            bookings.add(b);
                        }
                        callback.onSuccess(bookings);
                    } catch (Exception e) {
                        callback.onError("Could not read bookings data");
                    }
                },
                error -> callback.onError("Bookings request failed"));

        getQueue(context).add(request);
    }

    private static RequestQueue queue;

    private static RequestQueue getQueue(Context context) {
        if (queue == null) {
            queue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return queue;
    }
}