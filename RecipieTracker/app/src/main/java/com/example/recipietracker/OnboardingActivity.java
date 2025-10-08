package com.example.recipietracker;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private OnboardingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        ImageView backButton = findViewById(R.id.backButton);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false); // This disables swiping between pages

        // --- THIS IS THE NEW LOGIC FOR THE BACK BUTTON ---
        backButton.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem > 0) {
                // If we are not on the first page, go to the previous page
                viewPager.setCurrentItem(currentItem - 1);
            } else {
                // If we are on the first page, finish this activity to go back
                finish();
            }
        });
        // ---------------------------------------------------

        // This listener updates the progress bar as the page changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int progress = (int) (((float) (position + 1) / adapter.getItemCount()) * 100);
                progressBar.setProgress(progress);
            }
        });
    }
}