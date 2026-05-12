package com.plantris.pastelist;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
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