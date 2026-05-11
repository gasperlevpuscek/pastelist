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

public final class AddSubtaskSheet {

    private AddSubtaskSheet() {
        // Utility class.
    }

    public interface OnSubtaskAddedListener {
        void onSubtaskAdded(SubtaskItem item);
    }

    public static void show(
            @NonNull AppCompatActivity activity,
            long parentTaskId,
            @NonNull OnSubtaskAddedListener listener
    ) {
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        ViewGroup root = activity.findViewById(android.R.id.content);
        View view = activity.getLayoutInflater().inflate(R.layout.add_subtask, root, false);
        dialog.setContentView(view);

        EditText subtaskNameInput = view.findViewById(R.id.subtaskTaskName);
        EditText subtaskDescriptionInput = view.findViewById(R.id.subtaskTaskDescription);
        ImageButton subtaskAddButton = view.findViewById(R.id.subtaskAdd);

        subtaskAddButton.setOnClickListener(v -> {
            String title = subtaskNameInput.getText().toString().trim();
            String description = subtaskDescriptionInput.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(activity, "Subtask name must not be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            long newId;
            try (DatabaseInsert dbHelper = new DatabaseInsert(activity)) {
                newId = dbHelper.insertSubtask(parentTaskId, title, description, false);
            }

            listener.onSubtaskAdded(new SubtaskItem(newId, parentTaskId, title, description, false));
            dialog.dismiss();
        });

        dialog.show();

        subtaskNameInput.post(() -> {
            subtaskNameInput.requestFocus();
            InputMethodManager imm =
                    (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(subtaskNameInput, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }
}
