package com.plantris.pastelist;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final ArrayList<TodoItem> todoList = new ArrayList<>();
    private TodoAdapter adapter;
    private boolean showCompletedOnly = false;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_view);

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
        AddTaskSheet.show(this, showCompletedOnly, item -> {
            todoList.add(item);
            adapter.notifyItemInserted(todoList.size() - 1);
        });
    }
}