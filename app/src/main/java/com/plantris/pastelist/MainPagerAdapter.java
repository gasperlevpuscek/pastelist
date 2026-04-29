package com.plantris.pastelist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(@NonNull AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new UpcomingFragment();
        }
        if (position == 2) {
            return new SettingsFragment();
        }
        return new TaskFragment();
    }




    @Override
    public int getItemCount() {
        return 3;
    }
}

