package com.plantris.pastelist;

import static android.view.WindowInsetsController.APPEARANCE_LIGHT_CAPTION_BARS;
import static android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
import static android.view.WindowInsetsController.APPEARANCE_TRANSPARENT_CAPTION_BAR_BACKGROUND;
import static java.security.AccessController.getContext;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set status bar color and make icons dark for visibility on white background
        getWindow().setStatusBarColor(ContextCompat.getColor(this, android.R.color.white));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().getInsetsController().setSystemBarsAppearance(APPEARANCE_LIGHT_NAVIGATION_BARS,APPEARANCE_LIGHT_NAVIGATION_BARS);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        MaterialButton btnTasks = findViewById(R.id.btnTasks);
        MaterialButton btnUpcoming = findViewById(R.id.btnUpcoming);
        MaterialButton btnSettings = findViewById(R.id.btnPreferences);

        viewPager.setAdapter(new MainPagerAdapter(this));
        viewPager.setUserInputEnabled(false);

        btnTasks.setOnClickListener(v -> viewPager.setCurrentItem(0, false));
        btnUpcoming.setOnClickListener(v -> viewPager.setCurrentItem(1, false));
        btnSettings.setOnClickListener(v -> viewPager.setCurrentItem(2, false));

        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        100
                );
            }
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "task_reminders",
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager == null) {
                Log.w("MainActivity", "NotificationManager unavailable; channel not created");
                return;
            }
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Permanently remove tasks that have been marked completed while the app was running.
        try (DatabaseInsert dbHelper = new DatabaseInsert(this)) {
            dbHelper.deleteCompletedEntries();
        } catch (Exception ignored) {
            // ignore errors during cleanup
        }
    }
}