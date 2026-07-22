package com.example.myapplication;

import android.location.Location;

// Knows the geographic location of the marine ports the app serves and
// can work out which one the user is closest to. Used together with the
// device GPS (LocationManager) to auto-fill the "From" field.
public class PortLocator {

    public static class Port {
        final String name;
        final double lat;
        final double lng;

        Port(String name, double lat, double lng) {
            this.name = name;
            this.lat = lat;
            this.lng = lng;
        }
    }

    // Approximate coordinates of the ports used in the boats table.
    private static final Port[] PORTS = new Port[]{
            new Port("Dar es Salaam", -6.8235, 39.2695),
            new Port("Zanzibar", -6.1659, 39.1990),
            new Port("Mafia", -7.9167, 39.6667),
            new Port("Pemba", -5.0500, 39.7500),
            new Port("Mwanza", -2.5164, 32.9175),
            new Port("Bukoba", -1.3320, 31.8120)
    };

    // Returns {lat, lng} for a named port, or null if we don't know it.
    // Used by the map screen to plot the route.
    public static double[] coordsFor(String name) {
        if (name == null) {
            return null;
        }
        String query = name.trim();
        for (Port port : PORTS) {
            if (port.name.equalsIgnoreCase(query)) {
                return new double[]{port.lat, port.lng};
            }
        }
        return null;
    }

    // Returns the name of the port nearest to the given coordinates.
    public static String nearestPort(double lat, double lng) {
        Port nearest = null;
        double best = Double.MAX_VALUE;

        for (Port port : PORTS) {
            float[] result = new float[1];
            Location.distanceBetween(lat, lng, port.lat, port.lng, result);
            if (result[0] < best) {
                best = result[0];
                nearest = port;
            }
        }
        return nearest != null ? nearest.name : "Dar es Salaam";
    }
}