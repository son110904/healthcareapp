package com.example.healthcareapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.util.Calendar;

public class HomeFragment extends Fragment {

    private TextView stepCounterText;
    private Button resetButton, saveButton;
    private int stepCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        stepCounterText = view.findViewById(R.id.step_counter_text);
        resetButton = view.findViewById(R.id.reset_button);
        saveButton = view.findViewById(R.id.save_button);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepCount = 0;
                updateStepCounter();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Lưu số bước chân
                // Có thể thêm code để lưu vào cơ sở dữ liệu hoặc SharedPreferences
            }
        });

        // Thiết lập calendar view
        setupCalendarView(view);

        return view;
    }

    private void updateStepCounter() {
        stepCounterText.setText(String.valueOf(stepCount));
    }

    private void setupCalendarView(View view) {
        // Thiết lập calendar view
        // Phần này có thể sử dụng thư viện ngoài như MaterialCalendarView
    }
}