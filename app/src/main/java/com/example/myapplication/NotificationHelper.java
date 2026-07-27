package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID = "smart_marine_booking";
    private static final String CHANNEL_NAME = "Booking Notifications";
    private static final String CHANNEL_DESC = "Notifications for booking confirmations and updates";

    public static void showBookingConfirmed(Context context, String reference,
                                             String route, String date) {
        createChannel(context);

        String title = "Booking Confirmed!";
        String body = "Ref: " + reference + "\n" + route + " on " + date;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText("Ref: " + reference + " — " + route)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(
                    (int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException ignored) {
            // Permission not granted — silently skip
        }
    }

    private static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_DESC);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
