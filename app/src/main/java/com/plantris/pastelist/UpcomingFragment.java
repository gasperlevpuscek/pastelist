package com.plantris.pastelist;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UpcomingFragment extends Fragment {

    private static final DateTimeFormatter DB_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault());
    private static final DateTimeFormatter DB_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

    private UpcomingDateAdapter upcomingDateAdapter;
    private TaskFragment taskFragment;

    public void setTaskFragment(TaskFragment fragment) {
        this.taskFragment = fragment;
    }

    public UpcomingFragment() {
        super(R.layout.upcoming_task_view);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView upcomingTaskRecyclerView = view.findViewById(R.id.upcomingTaskRecyclerView);
        upcomingDateAdapter = new UpcomingDateAdapter();
        if (getContext() != null) {
            upcomingDateAdapter.setDatabaseInsert(new DatabaseInsert(getContext()));
        }
        upcomingDateAdapter.setOnTaskCompletedListener(() -> {
            loadUpcomingDays();
            if (taskFragment != null) {
                taskFragment.reloadTasks();
            }
        });
        upcomingTaskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        upcomingTaskRecyclerView.setAdapter(upcomingDateAdapter);

        loadUpcomingDays();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUpcomingDays();
    }


    private void loadUpcomingDays() {
        if (upcomingDateAdapter == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        ArrayList<LocalDate> daysInRange = new ArrayList<>();
        Map<LocalDate, List<TodoItem>> tasksByDate = new HashMap<>();
        Map<LocalDate, String> dateLabels = new HashMap<>();

        for (int i = 0; i < 93; i++) {
            LocalDate day = today.plusDays(i);
            daysInRange.add(day);
            tasksByDate.put(day, new ArrayList<>());

            // Add special labels for Today and Tomorrow
            if (i == 0) {
                dateLabels.put(day, "Today");
            } else if (i == 1) {
                dateLabels.put(day, "Tomorrow");
            }
        }

        if (getContext() != null) {
            try (DatabaseInsert dbHelper = new DatabaseInsert(getContext())) {
                for (TodoItem item : dbHelper.readAllEntries()) {
                    if (item.isCompleted()) {
                        continue;
                    }

                    LocalDate taskDate = parseDbDate(item.getDate());
                    if (taskDate == null || !tasksByDate.containsKey(taskDate)) {
                        continue;
                    }

                    List<TodoItem> dayTasks = tasksByDate.get(taskDate);
                    if (dayTasks != null) {
                        dayTasks.add(item);
                    }
                }
            }
        }

        for (List<TodoItem> dayTasks : tasksByDate.values()) {
            dayTasks.sort(Comparator.comparing(this::safeTaskTime));
        }


        upcomingDateAdapter.setData(daysInRange, tasksByDate, dateLabels);
    }

    @Nullable
    private LocalDate parseDbDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(rawDate.trim(), DB_DATE_FORMATTER);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    @NonNull
    private LocalTime safeTaskTime(@NonNull TodoItem item) {
        String rawTime = item.getTime();
        if (rawTime == null || rawTime.trim().isEmpty()) {
            return LocalTime.MAX;
        }
        try {
            return LocalTime.parse(rawTime.trim(), DB_TIME_FORMATTER);
        } catch (DateTimeParseException ignored) {
            return LocalTime.MAX;
        }
    }
}

