package com.example.recipietracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class GenderFragment extends Fragment {
    private OnboardingViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_gender, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggleGroupGender);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.buttonFemale) {
                    viewModel.setGender("Female");
                } else if (checkedId == R.id.buttonMale) {
                    viewModel.setGender("Male");
                } else if (checkedId == R.id.buttonOther) {
                    viewModel.setGender("Other");
                }
            }
        });

        Button nextButton = view.findViewById(R.id.buttonNext);
        ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);
        nextButton.setOnClickListener(v -> viewPager.setCurrentItem(viewPager.getCurrentItem() + 1));
    }
}