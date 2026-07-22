package com.example.myapplication;

// A simple data holder (model) for one boat/ferry schedule.
// One row from the "boats" table becomes one Boat object.
public class Boat {
    public int id;
    public String name;
    public String origin;
    public String destination;
    public String departureTime;
    public String arrivalTime;
    public int price;

    // Helper to show the price nicely, e.g. "TZS 35,000".
    public String getPriceText() {
        return "TZS " + String.format(java.util.Locale.US, "%,d", price);
    }
}