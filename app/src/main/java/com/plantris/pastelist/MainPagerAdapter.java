package com.plantris.pastelist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MainPagerAdapter extends FragmentStateAdapter {

    private TaskFragment taskFragment;
    private UpcomingFragment upcomingFragment;

    public MainPagerAdapter(@NonNull AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            upcomingFragment = new UpcomingFragment();
            if (taskFragment != null) {
                upcomingFragment.setTaskFragment(taskFragment);
            }
            return upcomingFragment;
        }
        if (position == 2) {
            return new SettingsFragment();
        }
        taskFragment = new TaskFragment();
        if (upcomingFragment != null) {
            upcomingFragment.setTaskFragment(taskFragment);
        }
        return taskFragment;
    }




    @Override
    public int getItemCount() {
        return 3;
    }
}

