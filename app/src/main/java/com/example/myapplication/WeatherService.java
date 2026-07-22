package com.example.myapplication;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Locale;

// Fetches live marine weather from the OpenWeatherMap API using the
// Volley HTTP library (both required by the proposal). It works by city
// name (the port the user is travelling from).
//
//  >>> PASTE YOUR FREE API KEY BELOW <<<
//  Get one at https://openweathermap.org/api  ("Current Weather Data").
public class WeatherService {

    // TODO: replace with your own key. Until then, weather calls will
    // fail gracefully and the app keeps working.
    private static final String API_KEY = "2f6d44ac2ed6d6341dd403b06a9cbf0b";

    private static final String BASE_URL =
            "https://api.openweathermap.org/data/2.5/weather";

    // The screen implements this to receive the result on the main thread.
    public interface WeatherCallback {
        void onSuccess(double tempCelsius, String condition, boolean goodForTravel);
        void onError(String message);
    }

    // Returns true only when a real key has been set.
    public static boolean hasApiKey() {
        return !API_KEY.equals("YOUR_OPENWEATHERMAP_API_KEY") && !API_KEY.isEmpty();
    }

    public static void fetchByCity(Context context, String city, WeatherCallback callback) {
        if (!hasApiKey()) {
            callback.onError("No weather API key set");
            return;
        }

        String url = BASE_URL + "?q=" + android.net.Uri.encode(city)
                + "&units=metric&appid=" + API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject main = response.getJSONObject("main");
                        double temp = main.getDouble("temp");
                        JSONObject weather0 = response.getJSONArray("weather").getJSONObject(0);
                        String condition = weather0.getString("main");

                        // Simple rule: rough seas / storms are bad for boats.
                        String lower = condition.toLowerCase(Locale.US);
                        boolean good = !(lower.contains("storm")
                                || lower.contains("rain")
                                || lower.contains("thunder")
                                || lower.contains("squall"));

                        callback.onSuccess(temp, condition, good);
                    } catch (Exception e) {
                        callback.onError("Could not read weather data");
                    }
                },
                error -> callback.onError("Weather request failed"));

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