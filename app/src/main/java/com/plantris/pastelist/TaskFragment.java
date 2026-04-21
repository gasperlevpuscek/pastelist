package com.plantris.pastelist;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;

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
    private PopupWindow deleteTaskPopup;

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
                this::showDeleteTaskPopup
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

    private void showDeleteTaskPopup(TodoItem item, int position, View anchorView) {
        if (getContext() == null || getView() == null) {
            return;
        }

        if (deleteTaskPopup != null && deleteTaskPopup.isShowing()) {
            deleteTaskPopup.dismiss();
        }

        ViewGroup root = (ViewGroup) getView();
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.delete_task_popup, root, false);
        deleteTaskPopup = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        deleteTaskPopup.setOutsideTouchable(true);
        deleteTaskPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        deleteTaskPopup.setElevation(12f);

        popupView.findViewById(R.id.btnNoKeep).setOnClickListener(v -> deleteTaskPopup.dismiss());

        ImageButton closeButton = popupView.findViewById(R.id.btnClose);
        closeButton.setOnClickListener(v -> deleteTaskPopup.dismiss());

        popupView.findViewById(R.id.btnYesDelete).setOnClickListener(v -> {
            if (getContext() == null) {
                return;
            }
            try (DatabaseInsert dbHelper = new DatabaseInsert(getContext())) {
                dbHelper.deleteEntry(item.getId());
            }
            loadTasks(showCompletedOnly);
            deleteTaskPopup.dismiss();
        });

        int verticalOffset = Math.round(8 * getResources().getDisplayMetrics().density);
        int[] anchorLocation = new int[2];
        anchorView.getLocationOnScreen(anchorLocation);
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int spaceBelow = screenHeight - (anchorLocation[1] + anchorView.getHeight());

        popupView.measure(
                View.MeasureSpec.makeMeasureSpec(getResources().getDisplayMetrics().widthPixels, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(screenHeight, View.MeasureSpec.AT_MOST)
        );

        int popupHeight = popupView.getMeasuredHeight();
        if (spaceBelow >= popupHeight + verticalOffset) {
            deleteTaskPopup.showAsDropDown(anchorView, 0, verticalOffset, Gravity.START);
        } else {
            deleteTaskPopup.showAsDropDown(anchorView, 0, -anchorView.getHeight() - popupHeight - verticalOffset, Gravity.START);
        }
    }
}
