package com.plantris.pastelist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UpcomingDateAdapter extends RecyclerView.Adapter<UpcomingDateAdapter.UpcomingDateViewHolder> {

    private final ArrayList<LocalDate> upcomingDates = new ArrayList<>();
    private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale.getDefault());
    private final DateTimeFormatter numberFormatter = DateTimeFormatter.ofPattern("dd", Locale.getDefault());
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault());

    public void setDates(@NonNull List<LocalDate> dates) {
        upcomingDates.clear();
        upcomingDates.addAll(dates);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UpcomingDateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_upcoming_task_date, parent, false);
        return new UpcomingDateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UpcomingDateViewHolder holder, int position) {
        LocalDate date = upcomingDates.get(position);

        holder.upcomingViewDay.setText(date.format(dayFormatter).toUpperCase(Locale.getDefault()));
        holder.upcomingViewDateNumber.setText(date.format(numberFormatter));
        holder.upcomingViewMonth.setText(date.format(monthFormatter).toUpperCase(Locale.getDefault()));
        holder.upcomingTaskContainer.removeAllViews();
    }

    @Override
    public int getItemCount() {
        return upcomingDates.size();
    }

    static class UpcomingDateViewHolder extends RecyclerView.ViewHolder {
        final TextView upcomingViewDay;
        final TextView upcomingViewDateNumber;
        final TextView upcomingViewMonth;
        final LinearLayout upcomingTaskContainer;

        UpcomingDateViewHolder(@NonNull View itemView) {
            super(itemView);
            upcomingViewDay = itemView.findViewById(R.id.upcomingViewDay);
            upcomingViewDateNumber = itemView.findViewById(R.id.upcomingViewDateNumber);
            upcomingViewMonth = itemView.findViewById(R.id.upcomingViewMonth);
            upcomingTaskContainer = itemView.findViewById(R.id.upcomingTaskContainer);
        }
    }
}

