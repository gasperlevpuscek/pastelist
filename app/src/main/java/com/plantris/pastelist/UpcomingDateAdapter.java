package com.plantris.pastelist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UpcomingDateAdapter extends RecyclerView.Adapter<UpcomingDateAdapter.UpcomingDateViewHolder> {

    private final ArrayList<LocalDate> upcomingDates = new ArrayList<>();
    private final Map<LocalDate, List<TodoItem>> tasksByDate = new java.util.HashMap<>();
    private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.getDefault());
    private final DateTimeFormatter numberFormatter = DateTimeFormatter.ofPattern("dd", Locale.getDefault());
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault());
    private DatabaseInsert databaseInsert;
    private OnTaskCompletedListener onTaskCompletedListener;

    public interface OnTaskCompletedListener {
        void onTaskCompleted();
    }

    public void setData(@NonNull List<LocalDate> dates, @NonNull Map<LocalDate, List<TodoItem>> groupedTasks) {
        upcomingDates.clear();
        upcomingDates.addAll(dates);
        tasksByDate.clear();
        tasksByDate.putAll(groupedTasks);
        notifyDataSetChanged();
    }

    public void setDatabaseInsert(DatabaseInsert databaseInsert) {
        this.databaseInsert = databaseInsert;
    }

    public void setOnTaskCompletedListener(OnTaskCompletedListener listener) {
        this.onTaskCompletedListener = listener;
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

        holder.upcomingViewDay.setText(date.format(dayFormatter)+",");
        holder.upcomingViewDateNumber.setText(date.format(numberFormatter));
        holder.upcomingViewMonth.setText(date.format(monthFormatter));
        holder.upcomingTaskContainer.removeAllViews();

        List<TodoItem> tasksForDate = tasksByDate.getOrDefault(date, Collections.emptyList());
        LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
        for (TodoItem task : tasksForDate) {
            View taskView = inflater.inflate(R.layout.item_upcoming_task, holder.upcomingTaskContainer, false);
            TextView taskName = taskView.findViewById(R.id.upcomingViewTaskName);
            TextView taskTime = taskView.findViewById(R.id.upcomingViewTaskTime);
            CheckBox taskCheckbox = taskView.findViewById(R.id.upcomingTaskViewCompleted);

            taskName.setText(task.getTitle());
            String time = task.getTime() == null ? "" : task.getTime();
            taskTime.setText(time);
            taskCheckbox.setChecked(task.isCompleted());

            // Set checkbox listener to update database
            taskCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (databaseInsert != null) {
                    databaseInsert.updateCompleted(task.getId(), isChecked);
                    task.setCompleted(isChecked);
                    if (onTaskCompletedListener != null) {
                        onTaskCompletedListener.onTaskCompleted();
                    }
                }
            });

            holder.upcomingTaskContainer.addView(taskView);
        }
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

