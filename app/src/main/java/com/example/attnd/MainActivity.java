package com.example.attnd;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.attnd.utils.AlarmHelper;
import com.example.attnd.viewmodel.MainViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private int systemBottomInset = 0;
    private int systemTopInset = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        
        // Apply theme from preferences
        android.content.SharedPreferences prefs = getSharedPreferences("AttndPrefs", android.content.Context.MODE_PRIVATE);
        String theme = prefs.getString("app_theme", "blue");
        switch (theme) {
            case "green": setTheme(R.style.Theme_ATTND_Green); break;
            case "orange":
            case "yellow": setTheme(R.style.Theme_ATTND_Orange); break;
            case "pink": setTheme(R.style.Theme_ATTND_Pink); break;
            case "purple": setTheme(R.style.Theme_ATTND_Purple); break;
            default: setTheme(R.style.Theme_ATTND); break;
        }

        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        WindowInsetsControllerCompat insetsController =
                new WindowInsetsControllerCompat(
                        getWindow(),
                        getWindow().getDecorView()
                );

        insetsController.setAppearanceLightStatusBars(true);
        insetsController.setAppearanceLightNavigationBars(true);

        final View statusBarBg = findViewById(R.id.status_bar_bg);
        final BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);

        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(bottomNav, navController);

        final View navHost = findViewById(R.id.nav_host_fragment);

        final MainViewModel viewModel =
                new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.getTimeTable().observe(this, rows -> {
            if (rows != null) {
                AlarmHelper.scheduleTimetableAlarms(this, rows);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(android.R.id.content),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    systemTopInset = systemBars.top;
                    systemBottomInset = systemBars.bottom;

                    ViewGroup.LayoutParams lp =
                            statusBarBg.getLayoutParams();

                    lp.height = systemTopInset;
                    statusBarBg.setLayoutParams(lp);

                    updatePaddings(
                            navController,
                            navHost,
                            bottomNav
                    );

                    return WindowInsetsCompat.CONSUMED;
                });

        navController.addOnDestinationChangedListener(
                (controller, destination, arguments) ->
                        updatePaddings(
                                controller,
                                navHost,
                                bottomNav
                        ));

        if (getIntent() != null
                && getIntent().getBooleanExtra(
                "OPEN_TIMETABLE",
                false
        )) {
            navController.navigate(R.id.nav_time_table);
        }

        if (getIntent() != null
                && getIntent().hasExtra(
                "WIDGET_CLASS_NAME"
        )) {

            String targetClass =
                    getIntent().getStringExtra(
                            "WIDGET_CLASS_NAME"
                    );

            Bundle bundle = new Bundle();
            bundle.putString(
                    "className",
                    targetClass
            );

            navController.navigate(
                    R.id.nav_attendance,
                    bundle
            );
        }
    }

    private void updatePaddings(
            NavController controller,
            View navHost,
            View bottomNav
    ) {

        if (navHost == null) return;
        if (controller.getCurrentDestination() == null) return;

        int id = controller.getCurrentDestination().getId();

        int topPadding = systemTopInset;
        int bottomPadding = 0;

        if (id == R.id.nav_attendance
                || id == R.id.nav_manage
                || id == R.id.nav_reports
                || id == R.id.nav_menu) {

            bottomNav.setVisibility(View.VISIBLE);
            bottomNav.setPadding(0, 0, 0, systemBottomInset);
            
        } else {
            bottomNav.setVisibility(View.GONE);
            bottomPadding = systemBottomInset;
        }

        navHost.setPadding(
                0,
                topPadding,
                0,
                bottomPadding
        );
    }
}