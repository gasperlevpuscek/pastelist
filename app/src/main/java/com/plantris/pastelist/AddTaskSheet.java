package com.plantris.pastelist;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

public final class AddTaskSheet {

    private AddTaskSheet() {
        // Utility class.
    }

    public interface OnTaskAddedListener {
        void onTaskAdded(TodoItem item);
    }

    public static void show(
            @NonNull AppCompatActivity activity,
            boolean showCompletedOnly,
            OnTaskAddedListener listener
    ) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        ViewGroup root = activity.findViewById(android.R.id.content);
        View view = activity.getLayoutInflater().inflate(R.layout.add_task, root, false);
        dialog.setContentView(view);

        EditText taskNameInput = view.findViewById(R.id.atTaskName);
        EditText taskDescriptionInput = view.findViewById(R.id.atTaskDescription);
        ImageButton btnAdd = view.findViewById(R.id.atTaskAdd);
        MaterialButton btnOpenDatePicker = view.findViewById(R.id.atTaskOpenDatePicker);

        final String[] selectedDate = {""};
        final String[] selectedTime = {""};

        btnOpenDatePicker.setOnClickListener(v ->
                DateSheet.show(activity, selectedDate[0], selectedTime[0], (date, time) -> {
                    selectedDate[0] = date;
                    selectedTime[0] = time;
                    btnOpenDatePicker.setText(formatDateTimeLabel(activity, selectedDate[0], selectedTime[0]));
                })
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

            long newRowId;
            try (DatabaseInsert dbHelper = new DatabaseInsert(activity)) {
                newRowId = dbHelper.insertEntry(title, description, date, time, false);
            }

            if (!showCompletedOnly && listener != null) {
                listener.onTaskAdded(new TodoItem(newRowId, title, description, date, time, false));
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
}
