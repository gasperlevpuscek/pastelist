package com.plantris.pastelist;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public final class ReminderSheet {

    private ReminderSheet() {
        // Utility class.
    }

    public interface OnReminderSelectedListener {
        void onReminderSelected(Integer reminderMinutesBefore);
    }

    public static void showReminder(
            @NonNull AppCompatActivity activity,
            @NonNull OnReminderSelectedListener listener
    ) {
        BottomSheetDialog reminderDialog = new BottomSheetDialog(activity);
        ViewGroup root = activity.findViewById(android.R.id.content);
        View reminderView = activity.getLayoutInflater().inflate(R.layout.add_task_reminder, root, false);
        reminderDialog.setContentView(reminderView);

        RadioGroup reminderOptions = reminderView.findViewById(R.id.reminderRadioGroup);
        Button btnSaveReminder = reminderView.findViewById(R.id.btnSaveReminder);

        // Default to "No reminder"
        if (reminderOptions.getChildCount() > 0) {
            ((RadioButton) reminderOptions.getChildAt(0)).setChecked(true);
        }

        btnSaveReminder.setOnClickListener(v -> {
            int selectedId = reminderOptions.getCheckedRadioButtonId();
            Integer reminderMinutesBefore = null;
            if (selectedId == R.id.reminder_due_time) {
                reminderMinutesBefore = 0;
            } else if (selectedId == R.id.reminder_1_hour) {
                reminderMinutesBefore = 60;
            } else if (selectedId == R.id.reminder_1_day) {
                reminderMinutesBefore = 1440;
            } else if (selectedId == R.id.reminder_custom) {
                reminderMinutesBefore = null;
            } else {
                reminderMinutesBefore = null;
            }
            listener.onReminderSelected(reminderMinutesBefore);
            reminderDialog.dismiss();
        });




        reminderDialog.show();
    }
}
