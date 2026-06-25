package com.example.attnd;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.example.attnd.database.ClassEntity;
import com.example.attnd.viewmodel.MainViewModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: classes4.dex */
public class WidgetConfigActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "com.example.attnd.WidgetPrefs";
    public static final String PREF_PREFIX_KEY = "appwidget_";
    private int appWidgetId = 0;

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(0);
        setContentView(R.layout.activity_widget_config);
        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        insetsController.setAppearanceLightStatusBars(true);
        insetsController.setAppearanceLightNavigationBars(true);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            this.appWidgetId = extras.getInt("appWidgetId", 0);
        }
        if (this.appWidgetId == 0) {
            finish();
            return;
        }
        final ListView lvClasses = (ListView) findViewById(R.id.lv_classes);
        MainViewModel viewModel = (MainViewModel) new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getAllClasses().observe(this, new Observer() { // from class: com.example.attnd.WidgetConfigActivity$$ExternalSyntheticLambda1
            @Override // androidx.lifecycle.Observer
            public final void onChanged(Object obj) {
                WidgetConfigActivity.this.m198lambda$onCreate$1$comexampleattndWidgetConfigActivity(lvClasses, (List) obj);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$onCreate$1$com-example-attnd-WidgetConfigActivity, reason: not valid java name */
    /* synthetic */ void m198lambda$onCreate$1$comexampleattndWidgetConfigActivity(ListView lvClasses, List classes) {
        final List<String> classNames = new ArrayList<>();
        Iterator it = classes.iterator();
        while (it.hasNext()) {
            ClassEntity c = (ClassEntity) it.next();
            classNames.add(c.className);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, classNames);
        lvClasses.setAdapter((ListAdapter) adapter);
        lvClasses.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.example.attnd.WidgetConfigActivity$$ExternalSyntheticLambda0
            @Override // android.widget.AdapterView.OnItemClickListener
            public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
                WidgetConfigActivity.this.m197lambda$onCreate$0$comexampleattndWidgetConfigActivity(classNames, adapterView, view, i, j);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$onCreate$0$com-example-attnd-WidgetConfigActivity, reason: not valid java name */
    /* synthetic */ void m197lambda$onCreate$0$comexampleattndWidgetConfigActivity(List classNames, AdapterView parent, View view, int position, long id) {
        String selectedClass = (String) classNames.get(position);
        handleClassSelection(selectedClass);
    }

    private void handleClassSelection(String className) {
        SharedPreferences.Editor prefs = getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + this.appWidgetId, className);
        prefs.apply();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        ClassWidgetProvider.updateAppWidget(this, appWidgetManager, this.appWidgetId);
        Intent resultValue = new Intent();
        resultValue.putExtra("appWidgetId", this.appWidgetId);
        setResult(-1, resultValue);
        finish();
    }
}
