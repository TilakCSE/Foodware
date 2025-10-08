package com.example.recipietracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

public class GoalFragment extends Fragment {
    private OnboardingViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_goal, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);

        RadioGroup radioGroup = view.findViewById(R.id.radioGroupGoal);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioEatBalanced) {
                viewModel.setGoal("Eat balanced");
            } else if (checkedId == R.id.radioLoseWeight) {
                viewModel.setGoal("Lose weight");
            } else if (checkedId == R.id.radioGainMuscle) {
                viewModel.setGoal("Gain muscle");
            }
        });

        Button nextButton = view.findViewById(R.id.buttonNext);
        ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);
        nextButton.setOnClickListener(v -> viewPager.setCurrentItem(viewPager.getCurrentItem() + 1));
    }
}