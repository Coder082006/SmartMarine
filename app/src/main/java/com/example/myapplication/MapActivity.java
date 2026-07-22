package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;

// Full-screen Route Map page. Opened from the "View Map" button on the
// Available Boats screen. It draws the sea route between the two ports on
// an OpenStreetMap map inside a WebView (using Leaflet). This needs no API
// key — it just needs an internet connection to load the map tiles.
public class MapActivity extends AppCompatActivity {

    WebView webView;
    TextView tvRouteHeader;
    TextView tvMapMessage;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainMap), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton btnBack = findViewById(R.id.btnBack);
        webView = findViewById(R.id.mapWebView);
        tvRouteHeader = findViewById(R.id.tvRouteHeader);
        tvMapMessage = findViewById(R.id.tvMapMessage);

        btnBack.setOnClickListener(v -> finish());

        String from = getIntent().getStringExtra("from");
        String to = getIntent().getStringExtra("to");
        tvRouteHeader.setText((from == null ? "" : from) + "  →  " + (to == null ? "" : to));

        double[] a = PortLocator.coordsFor(from);
        double[] b = PortLocator.coordsFor(to);

        if (a == null && b == null) {
            showMessage("Map not available for this route.");
            return;
        }
        // If only one port is known, centre the map on it.
        if (a == null) a = b;
        if (b == null) b = a;

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadDataWithBaseURL(
                "https://www.openstreetmap.org/",
                buildMapHtml(a, b, from, to),
                "text/html", "utf-8", null);
    }

    private void showMessage(String message) {
        webView.setVisibility(View.GONE);
        tvMapMessage.setVisibility(View.VISIBLE);
        tvMapMessage.setText(message);
    }

    // Builds a small Leaflet page that plots both ports and a line between
    // them on OpenStreetMap tiles.
    private String buildMapHtml(double[] a, double[] b, String from, String to) {
        String origin = from == null ? "Origin" : from.replace("'", " ");
        String dest = to == null ? "Destination" : to.replace("'", " ");

        return "<!DOCTYPE html><html><head>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>"
                + "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>"
                + "<style>html,body,#map{height:100%;margin:0;padding:0;}</style>"
                + "</head><body><div id='map'></div><script>"
                + String.format(Locale.US, "var a=[%f,%f];var b=[%f,%f];",
                        a[0], a[1], b[0], b[1])
                + "var map=L.map('map');"
                + "L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png',"
                + "{maxZoom:18,attribution:'© OpenStreetMap'}).addTo(map);"
                + "L.marker(a).addTo(map).bindPopup('" + origin + "');"
                + "L.marker(b).addTo(map).bindPopup('" + dest + "');"
                + "var line=L.polyline([a,b],{color:'#1565C0',weight:4,dashArray:'8,6'}).addTo(map);"
                + "if(a[0]===b[0]&&a[1]===b[1]){map.setView(a,11);}"
                + "else{map.fitBounds(line.getBounds().pad(0.4));}"
                + "</script></body></html>";
    }
}