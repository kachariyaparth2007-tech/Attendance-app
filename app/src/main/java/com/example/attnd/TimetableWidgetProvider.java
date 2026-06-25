package com.example.attnd;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.RemoteViews;
import com.example.attnd.database.AppDatabase;
import com.example.attnd.database.TimeTableEntity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class TimetableWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId) {
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_timetable);
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("OPEN_TIMETABLE", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, flags);
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            
            SharedPreferences userPrefs = context.getSharedPreferences("AttndPrefs", Context.MODE_PRIVATE);
            String tId = userPrefs.getString("userId", "");
            
            // Get data directly from DB (non-live)
            List<TimeTableEntity> rows = db.attendanceDao().getTimeTableList(tId);

            if (rows == null || rows.isEmpty()) {
                views.setTextViewText(R.id.widget_upcoming_class_name, "No Classes");
                views.setTextViewText(R.id.widget_upcoming_time, "Timetable is empty");
                appWidgetManager.updateAppWidget(appWidgetId, views);
                return;
            }

            Calendar calendar = Calendar.getInstance();
            int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.getTime());

            String nextClass = "Done for today!";
            String nextTime = "No more lectures";
            long minTimeDiff = Long.MAX_VALUE;

            for (TimeTableEntity row : rows) {
                String todayClass = getClassNameForDay(row, currentDay);
                if (todayClass != null && !todayClass.equals("-") && !todayClass.trim().isEmpty()) {
                    // Check if lecture is upcoming today
                    if (row.startTime.compareTo(currentTime) >= 0) {
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                            Date classDate = sdf.parse(row.startTime);
                            Date currentDate = sdf.parse(currentTime);
                            
                            if (classDate != null && currentDate != null) {
                                long diff = classDate.getTime() - currentDate.getTime();
                                if (diff < minTimeDiff) {
                                    minTimeDiff = diff;
                                    nextClass = todayClass;
                                    nextTime = formatTo12Hour(row.startTime);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            
            views.setTextViewText(R.id.widget_upcoming_class_name, nextClass);
            views.setTextViewText(R.id.widget_upcoming_time, nextTime);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        });
    }

    private static String getClassNameForDay(TimeTableEntity row, int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.MONDAY: return row.mondayClass;
            case Calendar.TUESDAY: return row.tuesdayClass;
            case Calendar.WEDNESDAY: return row.wednesdayClass;
            case Calendar.THURSDAY: return row.thursdayClass;
            case Calendar.FRIDAY: return row.fridayClass;
            case Calendar.SATURDAY: return row.saturdayClass;
            default: return "-";
        }
    }

    private static String formatTo12Hour(String time24) {
        try {
            SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = sdf24.parse(time24);
            if (date != null) return sdf12.format(date).toLowerCase();
        } catch (Exception e) {
            return time24;
        }
        return time24;
    }
}
