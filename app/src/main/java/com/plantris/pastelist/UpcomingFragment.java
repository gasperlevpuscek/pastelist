package com.plantris.pastelist;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.util.ArrayList;

public class UpcomingFragment extends Fragment {

    private UpcomingDateAdapter upcomingDateAdapter;

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

        loadNext7Days();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNext7Days();
    }

    private void loadNext7Days() {
        if (upcomingDateAdapter == null) {
            return;
        }

        LocalDate today = LocalDate.now();
        ArrayList<LocalDate> next7Days = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            next7Days.add(today.plusDays(i));
        }

        upcomingDateAdapter.setDates(next7Days);
    }
}

