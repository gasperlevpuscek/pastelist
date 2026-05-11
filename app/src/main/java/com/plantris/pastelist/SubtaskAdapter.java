package com.plantris.pastelist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SubtaskAdapter extends RecyclerView.Adapter<SubtaskAdapter.SubtaskVH> {

    private final ArrayList<SubtaskItem> items;

    public SubtaskAdapter(ArrayList<SubtaskItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public SubtaskVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subtask, parent, false);
        return new SubtaskVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubtaskVH holder, int position) {
        SubtaskItem item = items.get(position);

        holder.checkBoxCompleted.setOnCheckedChangeListener(null);
        holder.checkBoxCompleted.setChecked(item.isCompleted());
        holder.textViewTitle.setText(item.getTitle());
        bindOptionalText(holder.textViewDescription, item.getDescription());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(SubtaskItem item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
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

    static class SubtaskVH extends RecyclerView.ViewHolder {
        final CheckBox checkBoxCompleted;
        final TextView textViewTitle;
        final TextView textViewDescription;

        SubtaskVH(@NonNull View itemView) {
            super(itemView);
            checkBoxCompleted = itemView.findViewById(R.id.subtaskViewCompleted);
            textViewTitle = itemView.findViewById(R.id.subtaskViewTaskName);
            textViewDescription = itemView.findViewById(R.id.subtaskDescription);
        }
    }
}

