package com.plantris.pastelist;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class ReminderReceiver extends BroadcastReceiver {
    public static final String EXTRA_TASK_TITLE = "extra_task_title";
    public static final String EXTRA_DUE_DATE = "extra_due_date";
    public static final String EXTRA_DUE_TIME = "extra_due_time";
    public static final String EXTRA_REMINDER_MINUTES_BEFORE = "extra_reminder_minutes_before";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        ensureNotificationChannel(context);

        String taskTitle = intent != null ? intent.getStringExtra(EXTRA_TASK_TITLE) : null;
        int reminderMinutesBefore = intent != null
                ? intent.getIntExtra(EXTRA_REMINDER_MINUTES_BEFORE, -1)
                : -1;
        int notificationId = intent != null ? intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1001) : 1001;

        String reminderText = formatReminderOffset(reminderMinutesBefore);
        String safeTaskTitle = taskTitle == null || taskTitle.trim().isEmpty() ? "your task" : taskTitle.trim();

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, "task_reminders")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("You have tasks due")
                        .setContentText("You have a task \"" + safeTaskTitle + "\" due \"" + reminderText + "\"")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("You have a task \"" + safeTaskTitle + "\" due \"" + reminderText + "\""))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

    private void ensureNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) {
                return;
            }

            NotificationChannel channel = new NotificationChannel(
                    "task_reminders",
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }
    }

    private String formatReminderOffset(int reminderMinutesBefore) {
        if (reminderMinutesBefore < 0) {
            return "unknown time";
        }
        if (reminderMinutesBefore == 0) {
            return "now";
        }
        if (reminderMinutesBefore % 1440 == 0) {
            int days = reminderMinutesBefore / 1440;
            return days == 1 ? "in 1 day" : days + " days";
        }
        if (reminderMinutesBefore % 60 == 0) {
            int hours = reminderMinutesBefore / 60;
            return hours == 1 ? "in 1 hour" : hours + " hours";
        }
        return reminderMinutesBefore == 1 ? "1 minute" : reminderMinutesBefore + " minutes";
    }

}
