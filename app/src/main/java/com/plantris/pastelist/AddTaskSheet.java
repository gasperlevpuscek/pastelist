package com.plantris.pastelist;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public final class AddTaskSheet {

    private static final String TAG = "AddTaskSheet";

    private AddTaskSheet() {
        // Utility class.
    }



    public interface OnTaskAddedListener {
        void onTaskAdded(TodoItem item);
    }

    public interface OnTaskSavedListener {
        void onTaskSaved(TodoItem item, boolean isNewTask);
    }

    public static void show(
            @NonNull AppCompatActivity activity,
            boolean showCompletedOnly,
            OnTaskAddedListener listener
    ) {
        show(activity, showCompletedOnly, null, (item, isNewTask) -> {
            if (isNewTask && listener != null) {
                listener.onTaskAdded(item);
            }
        });
    }

    public static void show(
            @NonNull AppCompatActivity activity,
            boolean showCompletedOnly,
            @Nullable TodoItem itemToEdit,
            @Nullable OnTaskSavedListener listener
    ) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        ViewGroup root = activity.findViewById(android.R.id.content);
        View view = activity.getLayoutInflater().inflate(R.layout.add_task, root, false);
        dialog.setContentView(view);

        EditText taskNameInput = view.findViewById(R.id.atTaskName);
        EditText taskDescriptionInput = view.findViewById(R.id.atTaskDescription);
        ImageButton btnAdd = view.findViewById(R.id.atTaskAdd);
        MaterialButton btnOpenDatePicker = view.findViewById(R.id.atTaskOpenDatePicker);
        MaterialButton btnOpenReminderPicker = view.findViewById(R.id.atTaskReminder);

        final boolean isEditing = itemToEdit != null;
        final String[] selectedDate = {isEditing ? safe(itemToEdit.getDate()) : ""};
        final String[] selectedTime = {isEditing ? safe(itemToEdit.getTime()) : ""};
        final Integer[] selectedReminderMinutesBefore = {isEditing ? itemToEdit.getReminderMinutesBefore() : null};

        if (isEditing) {
            taskNameInput.setText(safe(itemToEdit.getTitle()));
            taskDescriptionInput.setText(safe(itemToEdit.getDescription()));
            btnOpenDatePicker.setText(formatDateTimeLabel(activity, selectedDate[0], selectedTime[0]));
        }

        btnOpenDatePicker.setOnClickListener(v ->
                DateSheet.show(activity, selectedDate[0], selectedTime[0], (date, time) -> {
                    selectedDate[0] = date;
                    selectedTime[0] = time;
                    btnOpenDatePicker.setText(formatDateTimeLabel(activity, selectedDate[0], selectedTime[0]));
                })
        );

        btnOpenReminderPicker.setOnClickListener(v ->
                ReminderSheet.showReminder(activity, reminderMinutesBefore -> selectedReminderMinutesBefore[0] = reminderMinutesBefore)
        );


        btnAdd.setOnClickListener(v -> {
            String title = taskNameInput.getText().toString().trim();
            String description = taskDescriptionInput.getText().toString().trim();
            String date = selectedDate[0];
            String time = selectedTime[0];


            if (title.isEmpty()) {
                Toast.makeText(activity, "Title must not be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (date.isEmpty()) {
                time = "";
            }

            if (isEditing) {
                try (DatabaseInsert dbHelper = new DatabaseInsert(activity)) {
                    dbHelper.updateEntry(
                            itemToEdit.getId(),
                            title,
                            description,
                            date,
                            time,
                            selectedReminderMinutesBefore[0]
                    );
                }
                if (listener != null) {
                    listener.onTaskSaved(
                            new TodoItem(
                                    itemToEdit.getId(),
                                    title,
                                    description,
                                    date,
                                    time,
                                    selectedReminderMinutesBefore[0],
                                    itemToEdit.isCompleted()
                            ),
                            false
                    );
                }
            } else {
                long newRowId;
                try (DatabaseInsert dbHelper = new DatabaseInsert(activity)) {
                    newRowId = dbHelper.insertEntry(
                            title,
                            description,
                            date,
                            time,
                            false,
                            selectedReminderMinutesBefore[0]
                    );
                }

                if (!showCompletedOnly && listener != null) {
                    listener.onTaskSaved(
                            new TodoItem(
                                    newRowId,
                                    title,
                                    description,
                                    date,
                                    time,
                                    selectedReminderMinutesBefore[0],
                                    false
                            ),
                            true
                    );
                }
                if (!date.isEmpty() && !time.isEmpty()) {
                    scheduleReminder(
                            activity,
                            title,
                            date,
                            time,
                            selectedReminderMinutesBefore[0]
                    );
                }
            }
            dialog.dismiss();
        });

        dialog.show();

        taskNameInput.post(() -> {
            taskNameInput.requestFocus();
            InputMethodManager imm =
                    (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(taskNameInput, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private static String formatDateTimeLabel(@NonNull AppCompatActivity activity, String date, String time) {
        if (date == null || date.isEmpty()) {
            return activity.getString(R.string.date);
        }
        if (time == null || time.isEmpty()) {
            return date;
        }
        return date + " " + time;
    }

    @NonNull
    private static String safe(@Nullable String value) {
        return value == null ? "" : value;
    }

    @SuppressLint({"ExactAlarm", "ScheduleExactAlarm"})
    private static void scheduleReminder(
            Context context,
            String taskTitle,
            String date,
            String time,
            @Nullable Integer reminderMinutesBefore
    ) {
        try {
            if (reminderMinutesBefore == null || date.isEmpty() || time.isEmpty()) {
                return;
            }

            String dateText = date + " " + time;
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            Date parsedDate = sdf.parse(dateText);

            if (parsedDate == null) {
                return;
            }

            long triggerTime = parsedDate.getTime() - (reminderMinutesBefore * 60_000L);

            if (triggerTime <= System.currentTimeMillis()) {
                return;
            }

            int requestCode = Objects.hash(taskTitle, date, time, reminderMinutesBefore);
            Intent intent = new Intent(context, ReminderReceiver.class)
                    .setAction(TAG + ".REMINDER_" + requestCode)
                    .putExtra("extra_task_title", taskTitle)
                    .putExtra("extra_due_date", date)
                    .putExtra("extra_due_time", time)
                    .putExtra("extra_reminder_minutes_before", reminderMinutesBefore)
                    .putExtra("extra_notification_id", requestCode);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.w(TAG, "AlarmManager unavailable; reminder not scheduled");
                return;
            }

            if (canScheduleExactAlarms(alarmManager)) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            } else {
                Log.w(TAG, "Exact alarm access unavailable; using inexact fallback");
                alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to schedule reminder due to alarm permission restrictions", e);
        } catch (ParseException e) {
            Log.e(TAG, "Failed to parse reminder date/time", e);
        }
    }

    private static boolean canScheduleExactAlarms(AlarmManager alarmManager) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms();
    }



}

