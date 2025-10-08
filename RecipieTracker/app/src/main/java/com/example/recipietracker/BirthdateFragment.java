package com.example.recipietracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

public class BirthdateFragment extends Fragment {
    private OnboardingViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_birthdate, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);

        DatePicker datePicker = view.findViewById(R.id.datePicker);
        Button nextButton = view.findViewById(R.id.buttonNext);
        ViewPager2 viewPager = requireActivity().findViewById(R.id.viewPager);

        nextButton.setOnClickListener(v -> {
            String birthDate = datePicker.getDayOfMonth() + "/" + (datePicker.getMonth() + 1) + "/" + datePicker.getYear();
            viewModel.setBirthDate(birthDate);
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        });
    }
}