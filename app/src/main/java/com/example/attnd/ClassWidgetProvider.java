package com.example.attnd;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.RemoteViews;
import com.example.attnd.database.AppDatabase;
import com.example.attnd.database.ClassEntity;
import java.util.concurrent.Executors;

public class ClassWidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(WidgetConfigActivity.PREFS_NAME, Context.MODE_PRIVATE);
        final String className = prefs.getString(WidgetConfigActivity.PREF_PREFIX_KEY + appWidgetId, null);
        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_class);
        
        if (className == null) {
            views.setTextViewText(R.id.widget_class_name, "Deleted");
            views.setTextViewText(R.id.widget_status, "Remove this widget");
            views.setOnClickPendingIntent(R.id.widget_root, null);
            appWidgetManager.updateAppWidget(appWidgetId, views);
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            SharedPreferences userPrefs = context.getSharedPreferences("AttndPrefs", Context.MODE_PRIVATE);
            String tId = userPrefs.getString("userId", "");
            
            int classCount = db.attendanceDao().checkIfClassExists(className, tId);
            if (classCount == 0) {
                views.setTextViewText(R.id.widget_class_name, "Deleted");
                views.setTextViewText(R.id.widget_status, "Remove this widget");
                views.setOnClickPendingIntent(R.id.widget_root, null);
                appWidgetManager.updateAppWidget(appWidgetId, views);
                return;
            }

            views.setTextViewText(R.id.widget_class_name, className);
            ClassEntity classEntity = db.attendanceDao().getClassByName(className, tId);
            if (classEntity != null && classEntity.subject != null && !classEntity.subject.isEmpty()) {
                views.setTextViewText(R.id.widget_status, classEntity.subject);
            } else {
                views.setTextViewText(R.id.widget_status, "Tap for Attendance");
            }

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra("WIDGET_CLASS_NAME", className);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= PendingIntent.FLAG_IMMUTABLE;
            }
            
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, flags);
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        });
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (Intent.ACTION_DATE_CHANGED.equals(action)) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName thisWidget = new ComponentName(context, ClassWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    public static void onClassRenamed(Context context, String oldClassName, String newClassName) {
        SharedPreferences prefs = context.getSharedPreferences(WidgetConfigActivity.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, ClassWidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        boolean isWidgetUpdated = false;
        
        for (int appWidgetId : appWidgetIds) {
            String savedName = prefs.getString(WidgetConfigActivity.PREF_PREFIX_KEY + appWidgetId, null);
            if (savedName != null && savedName.equals(oldClassName)) {
                editor.putString(WidgetConfigActivity.PREF_PREFIX_KEY + appWidgetId, newClassName);
                isWidgetUpdated = true;
            }
        }
        
        if (isWidgetUpdated) {
            editor.apply();
            Intent intent = new Intent(context, ClassWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            context.sendBroadcast(intent);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(WidgetConfigActivity.PREFS_NAME, Context.MODE_PRIVATE).edit();
        for (int appWidgetId : appWidgetIds) {
            prefs.remove(WidgetConfigActivity.PREF_PREFIX_KEY + appWidgetId);
        }
        prefs.apply();
    }
}
