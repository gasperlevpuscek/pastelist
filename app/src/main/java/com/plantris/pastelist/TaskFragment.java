package com.plantris.pastelist;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TaskFragment extends Fragment {

    private final ArrayList<TodoItem> todoList = new ArrayList<>();
    private TodoAdapter adapter;
    private boolean showCompletedOnly = false;

    public TaskFragment() {
        super(R.layout.task_view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        adapter = new TodoAdapter(
                todoList,
                (changedItem, isCompleted) -> {
                    if (getContext() == null) {
                        return;
                    }
                    try (DatabaseInsert dbHelper = new DatabaseInsert(getContext())) {
                        dbHelper.updateCompleted(changedItem.getId(), isCompleted);
                    }
                },
                this::showEditTaskPopup,
                null
        );

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        loadTasks(false);

        view.findViewById(R.id.add_task_button).setOnClickListener(v -> showAddTaskSheet());
        view.findViewById(R.id.switch_views).setOnClickListener(v -> loadTasks(!showCompletedOnly));
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadTasks(boolean completedOnly) {
        showCompletedOnly = completedOnly;
        todoList.clear();

        if (getContext() == null) {
            return;
        }

        try (DatabaseInsert dbHelper = new DatabaseInsert(getContext())) {
            for (TodoItem item : dbHelper.readAllEntries()) {
                if (item.isCompleted() == completedOnly) {
                    todoList.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void showAddTaskSheet() {
        if (!(requireActivity() instanceof AppCompatActivity)) {
            return;
        }

        AddTaskSheet.show((AppCompatActivity) requireActivity(), showCompletedOnly, item -> {
            todoList.add(item);
            adapter.notifyItemInserted(todoList.size() - 1);
        });
    }

    private void showEditTaskPopup(TodoItem item, int position) {
        if (!(requireActivity() instanceof AppCompatActivity) || getContext() == null) {
            return;
        }

        EditTask.show((AppCompatActivity) requireActivity(), item, new EditTask.OnTaskActionListener() {
            @Override
            public void onDuplicateRequested(@NonNull TodoItem sourceItem) {
                if (getContext() == null) {
                    return;
                }
                try (DatabaseInsert dbHelper = new DatabaseInsert(getContext())) {
                    dbHelper.insertEntry(
                            sourceItem.getTitle(),
                            sourceItem.getDescription(),
                            sourceItem.getDate(),
                            sourceItem.getTime(),
                            sourceItem.isCompleted()
                    );
                }
                loadTasks(showCompletedOnly);
            }

            @Override
            public void onDeleteConfirmed(@NonNull TodoItem sourceItem) {
                if (getContext() == null) {
                    return;
                }
                try (DatabaseInsert dbHelper = new DatabaseInsert(getContext())) {
                    dbHelper.deleteEntry(sourceItem.getId());
                }
                loadTasks(showCompletedOnly);
            }

            @Override
            public void onSaveRequested(@NonNull TodoItem sourceItem) {
                if (getContext() == null) {
                    return;
                }
                try (DatabaseInsert dbHelper = new DatabaseInsert(getContext())) {
                    dbHelper.updateEntry(
                            sourceItem.getId(),
                            sourceItem.getTitle(),
                            sourceItem.getDescription(),
                            sourceItem.getDate(),
                            sourceItem.getTime()
                    );
                }
                loadTasks(showCompletedOnly);
            }
        });
    }
}
