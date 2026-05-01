package com.plantris.pastelist;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
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
                (changedItem, isCompleted, position) -> {
                    if (getContext() == null) {
                        return;
                    }
                    // Mark as completed in DB (flag only)
                    try (DatabaseInsert dbHelper = new DatabaseInsert(getContext())) {
                        dbHelper.updateCompleted(changedItem.getId(), isCompleted);
                    }

                    // Show snackbar with Undo action. If undone, mark not completed and restore in list.
                    View parentView = requireView();
                    final int removedPos = position;
                    final TodoItem removedItem = changedItem;
                    Snackbar.make(parentView, "Task completed", Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> {
                                if (getContext() == null) return;
                                try (DatabaseInsert dbHelper = new DatabaseInsert(getContext())) {
                                    dbHelper.updateCompleted(removedItem.getId(), false);
                                }
                                // restore in-memory list and notify adapter
                                todoList.add(removedPos, removedItem);
                                adapter.notifyItemInserted(removedPos);
                            })
                            .show();
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

    public void reloadTasks() {
        loadTasks(showCompletedOnly);
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
                            sourceItem.isCompleted(),
                            sourceItem.getReminderMinutesBefore()
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
