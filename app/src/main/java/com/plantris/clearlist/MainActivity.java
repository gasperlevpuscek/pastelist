package com.plantris.clearlist;
import android.app.DatePickerDialog;
import java.util.Calendar;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final ArrayList<TodoItem> todoList = new ArrayList<>();
    private TodoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new TodoAdapter(todoList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.add_task_button).setOnClickListener(v -> showAddTaskSheet());
    }

    private void showAddTaskSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.add_task, null);
        dialog.setContentView(view);

        // VAR
        EditText taskNameInput = view.findViewById(R.id.atTaskName);
        EditText taskDescriptionInput = view.findViewById(R.id.atTaskDescription);
        EditText taskDateInput = view.findViewById(R.id.atTaskPickDate);
        ImageButton btnAdd = view.findViewById(R.id.atTaskAdd);


        dialog.show();

        // FOCUS TASK NAME INPUT
        taskNameInput.post(() -> {
            taskNameInput.requestFocus();
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(taskNameInput, InputMethodManager.SHOW_IMPLICIT);
        });


        // DATE
        taskDateInput.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();

            DatePickerDialog picker = new DatePickerDialog(
                    MainActivity.this,
                    (view1, year, month, day) -> {
                        String selected = String.format(Locale.getDefault(), "%02d.%02d.%d",
                                day, month + 1, year);

                        taskDateInput.setText(selected);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            picker.show();
        });

        // ADD TASK


        btnAdd.setOnClickListener(v -> {

            String title = taskNameInput.getText().toString().trim();
            String description = taskDescriptionInput.getText().toString().trim();
            String date = taskDateInput.getText().toString();

            if (title.isEmpty()) return;

            todoList.add(new TodoItem(title, description, date));
            adapter.notifyItemInserted(todoList.size() - 1);

            dialog.dismiss();
        });

    }
}