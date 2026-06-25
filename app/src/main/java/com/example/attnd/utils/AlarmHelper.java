package com.example.attnd.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.example.attnd.database.TimeTableEntity;
import com.example.attnd.receiver.LectureReminderReceiver;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AlarmHelper {
    private static final String TAG = "AlarmHelper";

    public static void scheduleTimetableAlarms(Context context, List<TimeTableEntity> rows) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        // Days of week mapping: Mon=2, Tue=3, Wed=4, Thu=5, Fri=6, Sat=7
        int[] calendarDays = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY};
        
        for (TimeTableEntity row : rows) {
            String[] classes = {
                row.mondayClass,
                row.tuesdayClass,
                row.wednesdayClass,
                row.thursdayClass,
                row.fridayClass,
                row.saturdayClass
            };
            
            for (int i = 0; i < classes.length; i++) {
                String className = classes[i];
                if (className != null && !className.equals("-") && !className.trim().isEmpty()) {
                    // Unique request code for each slot (row + day)
                    int requestCode = (row.id * 10) + i;
                    
                    Intent intent = new Intent(context, LectureReminderReceiver.class);
                    intent.putExtra("className", className);
                    intent.putExtra("time", formatTo12Hour(row.startTime));
                    
                    int flags = PendingIntent.FLAG_UPDATE_CURRENT;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        flags |= PendingIntent.FLAG_IMMUTABLE;
                    }
                    
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags);
                    
                    try {
                        String[] timeParts = row.startTime.split(":");
                        int targetHour = Integer.parseInt(timeParts[0]);
                        int targetMinute = Integer.parseInt(timeParts[1]);
                        
                        Calendar calendar = Calendar.getInstance();
                        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
                        int targetDay = calendarDays[i];
                        
                        // Calculate days until next occurrence
                        int daysToAdd = targetDay - currentDay;
                        if (daysToAdd < 0) {
                            daysToAdd += 7;
                        }
                        
                        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);
                        calendar.set(Calendar.HOUR_OF_DAY, targetHour);
                        calendar.set(Calendar.MINUTE, targetMinute);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        
                        // Set reminder 5 minutes before
                        calendar.add(Calendar.MINUTE, -5);
                        
                        // If it's already passed for today, schedule for next week
                        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                            calendar.add(Calendar.DAY_OF_YEAR, 7);
                        }
                        
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (alarmManager.canScheduleExactAlarms()) {
                                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                            } else {
                                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                            }
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        } else {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                        }
                        
                        Log.d(TAG, "Scheduled reminder for " + className + " at " + calendar.getTime());
                    } catch (Exception e) {
                        Log.e(TAG, "Error scheduling alarm: " + e.getMessage());
                    }
                }
            }
        }
    }

    private static String formatTo12Hour(String time24) {
        try {
            SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = sdf24.parse(time24);
            if (date != null) return sdf12.format(date).toLowerCase();
        } catch (Exception ignored) {}
        return time24;
    }
}
