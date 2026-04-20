package com.plantris.pastelist;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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
    private int selectedRangeDays = 7;

    public UpcomingFragment() {
        super(R.layout.upcoming_task_view);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView upcomingTaskRecyclerView = view.findViewById(R.id.upcomingTaskRecyclerView);
        upcomingDateAdapter = new UpcomingDateAdapter();
        upcomingTaskRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        upcomingTaskRecyclerView.setAdapter(upcomingDateAdapter);

        Spinner filterSpinner = view.findViewById(R.id.filterSpinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.upcoming_filter_options,
                android.R.layout.simple_spinner_item
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);
        filterSpinner.setSelection(0, false);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRangeDays = daysFromFilterPosition(position);
                loadUpcomingDays(selectedRangeDays);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRangeDays = 7;
            }
        });

        loadUpcomingDays(selectedRangeDays);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUpcomingDays(selectedRangeDays);
    }

    private int daysFromFilterPosition(int position) {
        switch (position) {
            case 1:
                return 31;
            case 2:
                return 62;
            case 3:
                return 93;
            default:
                return 7;
        }
    }

    private void loadUpcomingDays(int dayCount) {
        if (upcomingDateAdapter == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        ArrayList<LocalDate> daysInRange = new ArrayList<>();
        Map<LocalDate, List<TodoItem>> tasksByDate = new HashMap<>();

        for (int i = 0; i < dayCount; i++) {
            LocalDate day = today.plusDays(i);
            daysInRange.add(day);
            tasksByDate.put(day, new ArrayList<>());
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

                    tasksByDate.get(taskDate).add(item);
                }
            }
        }

        for (List<TodoItem> dayTasks : tasksByDate.values()) {
            dayTasks.sort(Comparator.comparing(this::safeTaskTime));
        }

        upcomingDateAdapter.setData(daysInRange, tasksByDate);
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

