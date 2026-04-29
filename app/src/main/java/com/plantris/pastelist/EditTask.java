package com.plantris.pastelist;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public final class EditTask {

    public interface OnTaskActionListener {
        void onDuplicateRequested(@NonNull TodoItem item);

        void onDeleteConfirmed(@NonNull TodoItem item);

        void onSaveRequested(@NonNull TodoItem item);
    }

    private EditTask() {
        // Utility class.
    }

    public static void show(
            @NonNull AppCompatActivity activity,
            @NonNull TodoItem item,
            @NonNull OnTaskActionListener listener
    ) {
        BottomSheetDialog editDialog = new BottomSheetDialog(activity);
        ViewGroup root = activity.findViewById(android.R.id.content);
        View editView = activity.getLayoutInflater().inflate(R.layout.edit_task, root, false);
        editDialog.setContentView(editView);
        editDialog.setCanceledOnTouchOutside(true);

        EditText taskNameInput = editView.findViewById(R.id.edTTaskName);
        EditText taskDescriptionInput = editView.findViewById(R.id.edTTaskDescription);
        Button btnDate = editView.findViewById(R.id.btnDate);
        Button btnSave = editView.findViewById(R.id.btnSave);
        ImageButton btnTaskMenu = editView.findViewById(R.id.btnTaskMenu);

        taskNameInput.setText(safe(item.getTitle()));
        taskDescriptionInput.setText(safe(item.getDescription()));

        final String[] selectedDate = {safe(item.getDate())};
        final String[] selectedTime = {safe(item.getTime())};
        btnDate.setText(formatDateLabel(selectedDate[0], selectedTime[0]));

        btnDate.setOnClickListener(v -> DateSheet.show(
                activity,
                selectedDate[0],
                selectedTime[0],
                (date, time) -> {
                    selectedDate[0] = safe(date);
                    selectedTime[0] = safe(time);
                    btnDate.setText(formatDateLabel(selectedDate[0], selectedTime[0]));
                }
        ));

        btnSave.setOnClickListener(v -> {
            TodoItem updatedItem = new TodoItem(
                    item.getId(),
                    taskNameInput.getText().toString().trim(),
                    taskDescriptionInput.getText().toString().trim(),
                    selectedDate[0],
                    selectedTime[0],
                    item.getReminderMinutesBefore(),
                    item.isCompleted()
            );
            listener.onSaveRequested(updatedItem);
        });

        btnTaskMenu.setOnClickListener(v -> showOptionsSheet(activity, root, item, listener, editDialog));

        editDialog.show();
    }

    private static void showOptionsSheet(
            @NonNull AppCompatActivity activity,
            @NonNull ViewGroup root,
            @NonNull TodoItem item,
            @NonNull OnTaskActionListener listener,
            @NonNull BottomSheetDialog editDialog
    ) {
        BottomSheetDialog optionsDialog = new BottomSheetDialog(activity);
        View optionsView = activity.getLayoutInflater().inflate(R.layout.edit_task_options, root, false);
        optionsDialog.setContentView(optionsView);
        optionsDialog.setCancelable(true);
        optionsDialog.setCanceledOnTouchOutside(true);

        Button duplicateButton = optionsView.findViewById(R.id.action_duplicate);
        Button deleteButton = optionsView.findViewById(R.id.action_delete);

        duplicateButton.setOnClickListener(v -> {
            listener.onDuplicateRequested(item);
            optionsDialog.dismiss();
            editDialog.dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            optionsDialog.dismiss();
            showDeleteSheet(activity, root, item, listener, editDialog);
        });

        optionsDialog.show();
    }

    private static void showDeleteSheet(
            @NonNull AppCompatActivity activity,
            @NonNull ViewGroup root,
            @NonNull TodoItem item,
            @NonNull OnTaskActionListener listener,
            @NonNull BottomSheetDialog editDialog
    ) {
        BottomSheetDialog deleteDialog = new BottomSheetDialog(activity);
        View deleteView = activity.getLayoutInflater().inflate(R.layout.delete_task, root, false);
        deleteDialog.setContentView(deleteView);
        deleteDialog.setCancelable(true);
        deleteDialog.setCanceledOnTouchOutside(true);

        Button cancelButton = deleteView.findViewById(R.id.button_cancel);
        Button confirmDeleteButton = deleteView.findViewById(R.id.button_delete);

        cancelButton.setOnClickListener(v -> deleteDialog.dismiss());

        confirmDeleteButton.setOnClickListener(v -> {
            listener.onDeleteConfirmed(item);
            deleteDialog.dismiss();
            editDialog.dismiss();
        });

        deleteDialog.show();
    }

    @NonNull
    private static String formatDateLabel(String date, String time) {
        if (date == null || date.isEmpty()) {
            return "Date";
        }
        if (time == null || time.isEmpty()) {
            return date;
        }
        return date + " " + time;
    }

    @NonNull
    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
