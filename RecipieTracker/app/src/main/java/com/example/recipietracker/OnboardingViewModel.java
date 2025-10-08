package com.example.recipietracker;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class OnboardingViewModel extends ViewModel {
    // Using MutableLiveData to hold the data
    public final MutableLiveData<String> goal = new MutableLiveData<>();
    public final MutableLiveData<String> gender = new MutableLiveData<>();
    public final MutableLiveData<String> birthDate = new MutableLiveData<>();
    public final MutableLiveData<String> skillLevel = new MutableLiveData<>();
    public final MutableLiveData<String> dietType = new MutableLiveData<>();
    public final MutableLiveData<List<String>> allergies = new MutableLiveData<>(new ArrayList<>());

    // Methods to update the data from each fragment
    public void setGoal(String selectedGoal) {
        goal.setValue(selectedGoal);
    }

    public void setGender(String selectedGender) {
        gender.setValue(selectedGender);
    }

    public void setBirthDate(String selectedBirthDate) {
        birthDate.setValue(selectedBirthDate);
    }

    public void setSkillLevel(String selectedSkill) {
        skillLevel.setValue(selectedSkill);
    }

    public void setDietType(String selectedDiet) {
        dietType.setValue(selectedDiet);
    }

    public void updateAllergies(String allergy, boolean isChecked) {
        List<String> currentAllergies = allergies.getValue();
        if (isChecked) {
            if (!currentAllergies.contains(allergy)) {
                currentAllergies.add(allergy);
            }
        } else {
            currentAllergies.remove(allergy);
        }
        allergies.setValue(currentAllergies);
    }
}