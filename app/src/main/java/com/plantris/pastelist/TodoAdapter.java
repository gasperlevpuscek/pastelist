package com.plantris.pastelist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoVH> {

    private final ArrayList<TodoItem> items;
    private final OnTodoCompletionChangedListener onTodoCompletionChangedListener;
    private final OnTodoItemClickListener onTodoItemClickListener;
    private final OnTodoItemLongClickListener onTodoItemLongClickListener;

    public interface OnTodoCompletionChangedListener {
        void onTodoCompletionChanged(TodoItem item, boolean isCompleted, int position);
    }

    public interface OnTodoItemClickListener {
        void onTodoItemClick(TodoItem item, int position);
    }

    public interface OnTodoItemLongClickListener {
        void onTodoItemLongClick(TodoItem item, int position, View anchorView);
    }

    public TodoAdapter(
            ArrayList<TodoItem> items,
            OnTodoCompletionChangedListener onTodoCompletionChangedListener,
            OnTodoItemClickListener onTodoItemClickListener,
            OnTodoItemLongClickListener onTodoItemLongClickListener
    ) {
        this.items = items;
        this.onTodoCompletionChangedListener = onTodoCompletionChangedListener;
        this.onTodoItemClickListener = onTodoItemClickListener;
        this.onTodoItemLongClickListener = onTodoItemLongClickListener;
    }

    @NonNull
    @Override
    public TodoVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo, parent, false);
        return new TodoVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoVH holder, int position) {
        TodoItem item = items.get(position);

        holder.textViewTitle.setText(item.getTitle());
        bindOptionalText(holder.textViewDescription, item.getDescription());
        bindOptionalTextWithIcon(holder.textViewDate, holder.imageViewDateIcon, item.getDate());
        bindOptionalTextWithIcon(holder.textViewTime, holder.imageViewTimeIcon, item.getTime());

        // Check if task is overdue and apply appropriate border and text
        boolean overdue = isOverdue(item);
        if (overdue) {
            holder.textViewOverdue.setText(R.string.overdue);
            holder.textViewOverdue.setTextColor(holder.itemView.getContext().getColor(R.color.pastelRed));
            holder.itemView.setBackground(AppCompatResources.getDrawable(holder.itemView.getContext(), R.drawable.item_border_overdue));
        } else {
            holder.textViewOverdue.setText("");
            holder.itemView.setBackground(AppCompatResources.getDrawable(holder.itemView.getContext(), R.drawable.item_border));
        }

        holder.checkBoxDone.setOnCheckedChangeListener(null);
        holder.checkBoxDone.setChecked(item.isCompleted());


        holder.checkBoxDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int adapterPos = holder.getBindingAdapterPosition();
            if (adapterPos != RecyclerView.NO_POSITION) {
                TodoItem changedItem = items.get(adapterPos);
                changedItem.setCompleted(isChecked);
                // notify parent (will update DB and show undo)
                if (onTodoCompletionChangedListener != null) {
                    onTodoCompletionChangedListener.onTodoCompletionChanged(changedItem, isChecked, adapterPos);
                }
                // remove from UI list
                items.remove(adapterPos);
                notifyItemRemoved(adapterPos);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            int adapterPos = holder.getBindingAdapterPosition();
            if (adapterPos != RecyclerView.NO_POSITION && onTodoItemClickListener != null) {
                onTodoItemClickListener.onTodoItemClick(items.get(adapterPos), adapterPos);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            int adapterPos = holder.getBindingAdapterPosition();
            if (adapterPos != RecyclerView.NO_POSITION && onTodoItemLongClickListener != null) {
                onTodoItemLongClickListener.onTodoItemLongClick(items.get(adapterPos), adapterPos, v);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void bindOptionalText(TextView textView, String value) {
        if (value == null || value.trim().isEmpty()) {
            textView.setText("");
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(value);
            textView.setVisibility(View.VISIBLE);
        }
    }

    private void bindOptionalTextWithIcon(TextView textView, ImageView iconView, String value) {
        if (value == null || value.trim().isEmpty()) {
            textView.setText("");
            textView.setVisibility(View.GONE);
            iconView.setVisibility(View.GONE);
        } else {
            textView.setText(value);
            textView.setVisibility(View.VISIBLE);
            iconView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Check if a task date is in the past (overdue).
     * Expected date format: "yyyy-MM-dd" or "dd/MM/yyyy"
     */
    private boolean isOverdue(TodoItem item) {
        if (item.isCompleted()) return false;
        if (item.getDate() == null || item.getTime() == null) return false;

        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d.M.yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            LocalDate date = LocalDate.parse(item.getDate(), dateFormatter);
            LocalTime time = LocalTime.parse(item.getTime(), timeFormatter);

            LocalDateTime taskDateTime = LocalDateTime.of(date, time);

            return taskDateTime.isBefore(LocalDateTime.now());
        } catch (Exception e) {
            return false;
        }
    }

    public static class TodoVH extends RecyclerView.ViewHolder {
        final CheckBox checkBoxDone;
        final TextView textViewTitle;
        final TextView textViewDescription;
        final ImageView imageViewDateIcon;
        final TextView textViewDate;
        final ImageView imageViewTimeIcon;
        final TextView textViewTime;
        final TextView textViewOverdue;

        TodoVH(@NonNull View itemView) {
            super(itemView);
            checkBoxDone = itemView.findViewById(R.id.checkBoxDone);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            imageViewDateIcon = itemView.findViewById(R.id.imageViewDateIcon);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            imageViewTimeIcon = itemView.findViewById(R.id.imageViewTimeIcon);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewOverdue = itemView.findViewById(R.id.textViewOverdue);

        }
    }




}