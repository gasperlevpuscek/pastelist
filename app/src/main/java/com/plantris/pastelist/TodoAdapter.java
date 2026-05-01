package com.plantris.pastelist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

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
        holder.textViewDescription.setText(item.getDescription());
        holder.textViewDate.setText(item.getDate());
        holder.textViewTime.setText(item.getTime());

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

    public static class TodoVH extends RecyclerView.ViewHolder {
        final CheckBox checkBoxDone;
        final TextView textViewTitle;
        final TextView textViewDescription;
        final TextView textViewDate;
        final TextView textViewTime;

        TodoVH(@NonNull View itemView) {
            super(itemView);
            checkBoxDone = itemView.findViewById(R.id.checkBoxDone);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewTime = itemView.findViewById(R.id.textViewTime);

        }
    }




}