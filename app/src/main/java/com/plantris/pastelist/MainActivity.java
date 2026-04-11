package com.plantris.pastelist;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        MaterialButton btnTasks = findViewById(R.id.btnTasks);
        MaterialButton btnUpcoming = findViewById(R.id.btnUpcoming);

        viewPager.setAdapter(new MainPagerAdapter(this));

        btnTasks.setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        btnUpcoming.setOnClickListener(v -> viewPager.setCurrentItem(1, true));
    }
}