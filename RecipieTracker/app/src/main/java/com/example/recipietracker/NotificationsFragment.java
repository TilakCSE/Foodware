package com.example.recipietracker;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import java.util.ArrayList;

public class NotificationsFragment extends Fragment {

    private OnboardingViewModel viewModel;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(getContext(), "Notifications enabled!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "You can enable notifications later in settings.", Toast.LENGTH_SHORT).show();
                }
                navigateToSignUp();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the shared ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(OnboardingViewModel.class);

        Button nextButton = view.findViewById(R.id.buttonNext);
        nextButton.setOnClickListener(v -> {
            // Check for notification permission (for Android 13+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                // For older versions, permission is granted by default, so just navigate
                navigateToSignUp();
            }
        });
    }

    private void navigateToSignUp() {
        Intent intent = new Intent(getActivity(), SignUpActivity.class);

        // Get all the data from the ViewModel and pass it to the SignUpActivity
        intent.putExtra("USER_GOAL", viewModel.goal.getValue());
        intent.putExtra("USER_GENDER", viewModel.gender.getValue());
        intent.putExtra("USER_BIRTHDATE", viewModel.birthDate.getValue());
        intent.putExtra("USER_SKILL", viewModel.skillLevel.getValue());
        intent.putExtra("USER_DIET", viewModel.dietType.getValue());
        if (viewModel.allergies.getValue() != null) {
            intent.putStringArrayListExtra("USER_ALLERGIES", new ArrayList<>(viewModel.allergies.getValue()));
        }

        startActivity(intent);
        getActivity().finish();
    }
}