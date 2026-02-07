package com.plantris.clearlist;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final ArrayList<TodoItem> todoList = new ArrayList<>();
    private TodoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new TodoAdapter(todoList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        findViewById(R.id.add_task_button).setOnClickListener(v -> showAddTaskSheet());
    }

    private void showAddTaskSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.add_task, null, false);

        EditText input = view.findViewById(R.id.text_input_field);
        ImageButton btnAdd = view.findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (text.isEmpty()) return;

            todoList.add(new TodoItem(text));
            adapter.notifyItemInserted(todoList.size() - 1);

            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.show();

        // optional: focus input
        input.requestFocus();
    }
}