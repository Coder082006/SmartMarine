package com.example.myapplication;

// A simple data holder (model) for one booked ticket.
// One row from the "bookings" table becomes one Booking object.
public class Booking {
    public String reference;
    public String passengerName;
    public String boatName;
    public String origin;
    public String destination;
    public String travelDate;
    public String departureTime;
    public int price;
    public String status;

    // e.g. "Dar es Salaam → Zanzibar"
    public String getRoute() {
        return origin + " → " + destination;
    }

    // e.g. "22/5/2026 · Azam Marine Ferry"
    public String getDetail() {
        return travelDate + " · " + boatName;
    }
}