package com.example.recipietracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

public class DietFragment extends Fragment {
    private OnboardingViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_diet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);

        RadioGroup radioGroup = view.findViewById(R.id.radioGroupDiet);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedButton = view.findViewById(checkedId);
            String diet = selectedButton.getText().toString().split("\n")[0];
            viewModel.setDietType(diet);
        });

        Button nextButton = view.findViewById(R.id.buttonNext);
        ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);
        nextButton.setOnClickListener(v -> viewPager.setCurrentItem(viewPager.getCurrentItem() + 1));
    }
}