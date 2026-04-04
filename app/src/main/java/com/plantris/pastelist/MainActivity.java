package com.plantris.pastelist;
import android.annotation.SuppressLint;
import java.util.Calendar;

import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final ArrayList<TodoItem> todoList = new ArrayList<>();
    private TodoAdapter adapter;
    private boolean showCompletedOnly = false;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new TodoAdapter(todoList, (changedItem, isCompleted) -> {
            try (DatabaseInsert dbHelper = new DatabaseInsert(this)) {
                dbHelper.updateCompleted(changedItem.getId(), isCompleted);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadTasks(false);

        findViewById(R.id.add_task_button).setOnClickListener(v -> showAddTaskSheet());
        findViewById(R.id.switch_views).setOnClickListener(v -> loadTasks(!showCompletedOnly));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadTasks(boolean completedOnly) {
        showCompletedOnly = completedOnly;
        todoList.clear();

        try (DatabaseInsert dbHelper = new DatabaseInsert(this)) {
            for (TodoItem item : dbHelper.readAllEntries()) {
                if (item.isCompleted() == completedOnly) {
                    todoList.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void showAddTaskSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.add_task, null);
        dialog.setContentView(view);

        // VAR
        EditText taskNameInput = view.findViewById(R.id.atTaskName);
        EditText taskDescriptionInput = view.findViewById(R.id.atTaskDescription);
        ImageButton btnAdd = view.findViewById(R.id.atTaskAdd);
        MaterialButton btnOpenDatePicker = view.findViewById(R.id.atTaskOpenDatePicker);

        final String[] selectedDate = {""};
        final String[] selectedTime = {""};

        dialog.show();

        // FOCUS TASK NAME INPUT
        taskNameInput.post(() -> {
            taskNameInput.requestFocus();
            InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(taskNameInput, InputMethodManager.SHOW_IMPLICIT);
        });


        btnOpenDatePicker.setOnClickListener(v -> {
            BottomSheetDialog dateDialog = new BottomSheetDialog(this);
            View dateView = getLayoutInflater().inflate(R.layout.add_task_date, null);
            dateDialog.setContentView(dateView);

            CalendarView calendarView = dateView.findViewById(R.id.calendarView);
            LinearLayout optionAddTime = dateView.findViewById(R.id.optionAddTime);
            TextView tvAddTime = dateView.findViewById(R.id.tvAddTime);
            Button btnSaveDate = dateView.findViewById(R.id.btnSaveDate);

            tvAddTime.setText(selectedTime[0].isEmpty() ? getString(R.string.add_time) : selectedTime[0]);

            calendarView.setOnDateChangeListener((cv, year, month, dayOfMonth) -> selectedDate[0] =
                    String.format(Locale.getDefault(), "%02d.%02d.%d", dayOfMonth, month + 1, year));

            optionAddTime.setOnClickListener(timeView -> {
                Calendar now = Calendar.getInstance();
                TimePickerDialog timePicker = new TimePickerDialog(
                        MainActivity.this,
                        (pickerView, hourOfDay, minute) -> {
                            selectedTime[0] = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                            tvAddTime.setText(selectedTime[0]);
                        },
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        true
                );
                timePicker.show();
            });

            btnSaveDate.setOnClickListener(saveView -> {
                btnOpenDatePicker.setText(formatDateTimeLabel(selectedDate[0], selectedTime[0]));
                dateDialog.dismiss();
            });

            dateDialog.show();
        });

        btnAdd.setOnClickListener(v -> {
            String title = taskNameInput.getText().toString().trim();
            String description = taskDescriptionInput.getText().toString().trim();
            String date = selectedDate[0];
            String time = selectedTime[0];

            if (title.isEmpty()){
                Toast.makeText(this, "Title must not be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            long newRowId;
            try (DatabaseInsert dbHelper = new DatabaseInsert(this)) {
                newRowId = dbHelper.insertEntry(
                        title,
                        description,
                        date,
                        time,
                        false
                );
            }

            if (!showCompletedOnly) {
                todoList.add(new TodoItem(newRowId, title, description, date, time, false));
                adapter.notifyItemInserted(todoList.size() - 1);
            }

            dialog.dismiss();
        });

    }

    private String formatDateTimeLabel(String date, String time) {
        if (date == null || date.isEmpty()) {
            return "";
        }
        if (time == null || time.isEmpty()) {
            return date;
        }
        return date + " " + time;
    }
}