package com.plantris.clearlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoVH> {

    private final ArrayList<TodoItem> items;

    public TodoAdapter(ArrayList<TodoItem> items) {
        this.items = items;
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

        holder.textViewTask.setText(item.getText());

        // IMPORTANT: reset checkbox state because RecyclerView reuses views
        holder.checkBoxDone.setOnCheckedChangeListener(null);
        holder.checkBoxDone.setChecked(false);

        holder.checkBoxDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                int adapterPos = holder.getBindingAdapterPosition();
                if (adapterPos != RecyclerView.NO_POSITION) {
                    items.remove(adapterPos);
                    notifyItemRemoved(adapterPos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class TodoVH extends RecyclerView.ViewHolder {
        CheckBox checkBoxDone;
        TextView textViewTask;

        TodoVH(@NonNull View itemView) {
            super(itemView);
            checkBoxDone = itemView.findViewById(R.id.checkBoxDone);
            textViewTask = itemView.findViewById(R.id.textViewTask);
        }
    }
}