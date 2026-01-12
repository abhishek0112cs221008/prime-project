package com.ek.primeproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private EditText editName, editEmail, inputInterest;
    private ChipGroup chipGroupInterests, chipGroupSuggestions;
    private Button btnAddInterest, btnSave, btnLogout;
    private List<String> myInterests = new ArrayList<>();
    private final String[] suggestions = { "Java", "Web Development", "Python", "Spring Boot", "React",
            "Machine Learning" };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        editName = view.findViewById(R.id.editName);
        editEmail = view.findViewById(R.id.editEmail);
        inputInterest = view.findViewById(R.id.inputInterest);
        chipGroupInterests = view.findViewById(R.id.chipGroupInterests);
        chipGroupSuggestions = view.findViewById(R.id.chipGroupSuggestions);
        btnAddInterest = view.findViewById(R.id.btnAddInterest);
        btnSave = view.findViewById(R.id.btnSave);
        btnLogout = view.findViewById(R.id.btnLogout);

        setupSuggestions();
        fetchProfile();

        btnAddInterest.setOnClickListener(v -> {
            String text = inputInterest.getText().toString().trim();
            if (!text.isEmpty()) {
                addInterest(text);
                inputInterest.setText("");
            }
        });

        btnSave.setOnClickListener(v -> saveProfile());

        btnLogout.setOnClickListener(v -> {
            new SessionManager(getContext()).logout();
            startActivity(new Intent(getContext(), LoginActivity.class));
            if (getActivity() != null)
                getActivity().finish();
        });

        return view;
    }

    private void setupSuggestions() {
        for (String suggestion : suggestions) {
            Chip chip = new Chip(getContext());
            chip.setText("+ " + suggestion);
            chip.setCheckable(false);
            chip.setClickable(true);
            chip.setChipBackgroundColorResource(R.color.card_bg);
            chip.setTextColor(getResources().getColor(R.color.white));
            chip.setOnClickListener(v -> addInterest(suggestion));
            chipGroupSuggestions.addView(chip);
        }
    }

    private void addInterest(String interest) {
        if (!myInterests.contains(interest)) {
            myInterests.add(interest);
            refreshInterestsChips();
        }
    }

    private void removeInterest(String interest) {
        myInterests.remove(interest);
        refreshInterestsChips();
    }

    private void refreshInterestsChips() {
        chipGroupInterests.removeAllViews();
        for (String interest : myInterests) {
            Chip chip = new Chip(getContext());
            chip.setText(interest);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> removeInterest(interest));
            chip.setChipBackgroundColorResource(R.color.accent_color);
            chip.setTextColor(getResources().getColor(R.color.white));
            chipGroupInterests.addView(chip);
        }
    }

    private void fetchProfile() {
        if (getContext() == null)
            return;
        RetrofitClient.getService(getContext()).getMe().enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    editName.setText(user.getName());
                    editEmail.setText(user.getEmail());

                    if (user.getInterests() != null && !user.getInterests().isEmpty()) {
                        myInterests = new ArrayList<>(Arrays.asList(user.getInterests().split(",")));
                        // Trim spaces
                        for (int i = 0; i < myInterests.size(); i++) {
                            myInterests.set(i, myInterests.get(i).trim());
                        }
                        refreshInterestsChips();
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String name = editName.getText().toString();
        String interestsStr = TextUtils.join(",", myInterests);

        User user = new User(name, interestsStr);

        RetrofitClient.getService(getContext()).updateProfile(user).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show();
                    // Update session name if changed
                    new SessionManager(getContext()).saveSessionName(name);
                } else {
                    Toast.makeText(getContext(), "Update Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
