package com.example.attnd.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.attnd.R;

public class LectureReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "lecture_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String className = intent.getStringExtra("className");
        String time = intent.getStringExtra("time");
        
        if (className == null || className.equals("-")) return;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Lecture Reminders", NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setContentTitle("Upcoming Lecture!")
                .setContentText("Your next lecture in " + className + " starts at " + time)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true);

        // Use a consistent ID based on class name to avoid spamming multiple notifications for same lecture
        int notificationId = className.hashCode();
        notificationManager.notify(notificationId, builder.build());
    }
}
