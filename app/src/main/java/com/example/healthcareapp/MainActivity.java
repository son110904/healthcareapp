package com.example.healthcareapp;
import com.example.healthcareapp.R;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // Hiển thị Trang chủ khi khởi động
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    if (item.getItemId() == R.id.nav_home) {
                        selectedFragment = new HomeFragment();
                    } else if (item.getItemId() == R.id.nav_emergency) {
                        selectedFragment = new EmergencyFragment();
                    }  else if (item.getItemId() == R.id.action_change_language) {
                        // Hiển thị dialog chọn ngôn ngữ
                        showChangeLanguageDialog();
                        return true; // Giữ mục hiện tại được chọn
                    }

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment)
                                .commit();
                    }

                    return true;
                }
            };
    public void openLanguageSettings(View view) {
        showChangeLanguageDialog();
    }
    private void showChangeLanguageDialog() {
        // Mảng lưu tên hiển thị của ngôn ngữ
        final String[] languages = {"Tiếng Việt", "English", "Español"};
        // Mảng lưu mã ngôn ngữ tương ứng
        final String[] languageCodes = {"vi", "en", "es"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ngôn ngữ / Choose language / Elegir idioma");

        // Đánh dấu ngôn ngữ hiện tại
        String currentLang = LocaleHelper.getLanguage(this);
        int checkedItem = 0; // Mặc định là tiếng Việt
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLang)) {
                checkedItem = i;
                break;
            }
        }

        builder.setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
            // Khi người dùng chọn ngôn ngữ
            String selectedLanguage = languageCodes[which];

            // Thay đổi ngôn ngữ
            LocaleHelper.setLocale(this, selectedLanguage);

            // Khởi động lại Activity hiện tại
            recreate();

            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase, LocaleHelper.getLanguage(newBase)));
    }
    }
