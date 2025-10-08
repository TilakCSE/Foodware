package com.example.recipietracker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class OnboardingAdapter extends FragmentStateAdapter {

    public OnboardingAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return a NEW fragment instance for the given position.
        switch (position) {
            case 0:
                return new GoalFragment();
            case 1:
                return new GenderFragment();
            case 2:
                return new BirthdateFragment();
            case 3:
                return new SkillFragment();
            case 4:
                return new DietFragment();
            case 5:
                return new AllergiesFragment();
            case 6:
                return new NotificationsFragment();
            default:
                return new GoalFragment(); // Default case
        }
    }

    @Override
    public int getItemCount() {
        // The total number of screens.
        return 7;
    }
}