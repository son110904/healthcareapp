package com.example.healthcareapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CallFragment extends Fragment {

    private LinearLayout customContactContainer;
    private static final int REQUEST_CALL_PERMISSION = 1;
    private List<Contact> contactList = new ArrayList<>(); // Danh sách liên hệ

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_call, container, false);

        customContactContainer = view.findViewById(R.id.customContactContainer);
        Button addContactButton = view.findViewById(R.id.addContactButton);

        if (addContactButton != null) {
            addContactButton.setOnClickListener(v -> showAddContactDialog());
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        }

        setupDefaultEmergencyCalls(view);
        loadContacts(); // Tải liên hệ đã lưu

        // Thiết lập nút quay lại
        View backButton = view.findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager().popBackStack();
            });
        }

        return view;
    }

    private void setupDefaultEmergencyCalls(View rootView) {
        String[] numbers = {"113", "114", "115"};
        LinearLayout layout = rootView.findViewById(R.id.contactListLayout);

        if (layout != null) {
            int count = layout.getChildCount();

            for (int i = 0; i < count; i++) {
                View child = layout.getChildAt(i);
                if (i < numbers.length) {
                    final String phone = numbers[i];
                    child.setOnClickListener(v -> {
                        makePhoneCall(phone);
                    });
                }
            }
        }
    }

    private void makePhoneCall(String phoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumber));
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
        } else {
            Toast.makeText(requireContext(), "Chưa được cấp quyền gọi!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAddContactDialog() {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 20, 40, 20);

        final EditText nameInput = new EditText(requireContext());
        nameInput.setHint("Tên liên hệ");
        nameInput.setBackgroundResource(R.drawable.edittext_background);
        layout.addView(nameInput);

        final EditText phoneInput = new EditText(requireContext());
        phoneInput.setHint("Số điện thoại");
        phoneInput.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneInput.setBackgroundResource(R.drawable.edittext_background);
        layout.addView(phoneInput);

        new AlertDialog.Builder(requireContext())
                .setTitle("Thêm liên hệ mới")
                .setView(layout)
                .setIcon(requireActivity().getDrawable(R.drawable.popup_background))
                .setPositiveButton("Thêm", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String phone = phoneInput.getText().toString().trim();
                    if (!name.isEmpty() && !phone.isEmpty()) {
                        LinearLayout newContact = createContactItem("📞", name, phone);
                        customContactContainer.addView(newContact);
                        contactList.add(new Contact(name, phone));
                        saveContacts();
                    } else {
                        Toast.makeText(requireContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private LinearLayout createContactItem(String iconText, String labelText, final String phoneNumber) {
        LinearLayout contactItem = new LinearLayout(requireContext());
        contactItem.setOrientation(LinearLayout.VERTICAL);
        contactItem.setGravity(Gravity.CENTER);
        contactItem.setBackgroundResource(R.drawable.rounded_contact_background);
        contactItem.setPadding(24, 24, 24, 24);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        contactItem.setLayoutParams(params);

        TextView icon = new TextView(requireContext());
        icon.setText(iconText);
        icon.setTextSize(40);
        icon.setGravity(Gravity.CENTER);
        icon.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        contactItem.addView(icon);

        TextView label = new TextView(requireContext());
        label.setText(labelText);
        label.setTextColor(requireContext().getResources().getColor(android.R.color.black));
        label.setTextSize(30);
        label.setGravity(Gravity.CENTER);
        label.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        contactItem.addView(label);

        contactItem.setOnClickListener(v -> {
            makePhoneCall(phoneNumber);
        });

        contactItem.setOnLongClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Tùy chọn")
                    .setItems(new CharSequence[]{"Chỉnh sửa", "Xóa"}, (dialog, which) -> {
                        int index = customContactContainer.indexOfChild(contactItem);
                        if (which == 0) {
                            showEditContactDialog(contactItem, iconText, labelText, phoneNumber);
                        } else if (which == 1) {
                            customContactContainer.removeView(contactItem);
                            contactList.remove(index);
                            saveContacts();
                        }
                    })
                    .show();
            return true;
        });

        return contactItem;
    }

    private void showEditContactDialog(LinearLayout contactItem, String iconText, String oldName, String oldPhone) {
        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 20, 40, 20);

        final EditText nameInput = new EditText(requireContext());
        nameInput.setText(oldName);
        nameInput.setBackgroundResource(R.drawable.edittext_background);
        layout.addView(nameInput);

        final EditText phoneInput = new EditText(requireContext());
        phoneInput.setText(oldPhone);
        phoneInput.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneInput.setBackgroundResource(R.drawable.edittext_background);
        layout.addView(phoneInput);

        new AlertDialog.Builder(requireContext())
                .setTitle("Chỉnh sửa liên hệ")
                .setView(layout)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newName = nameInput.getText().toString().trim();
                    String newPhone = phoneInput.getText().toString().trim();
                    if (!newName.isEmpty() && !newPhone.isEmpty()) {
                        int index = customContactContainer.indexOfChild(contactItem);
                        customContactContainer.removeView(contactItem);
                        LinearLayout updatedContact = createContactItem(iconText, newName, newPhone);
                        customContactContainer.addView(updatedContact, index);
                        contactList.set(index, new Contact(newName, newPhone));
                        saveContacts();
                    } else {
                        Toast.makeText(requireContext(), "Không được để trống!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveContacts() {
        if (getContext() == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("contacts", requireContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(contactList);
        editor.putString("contactList", json);
        editor.apply();
    }

    private void loadContacts() {
        if (getContext() == null) return;

        SharedPreferences prefs = requireContext().getSharedPreferences("contacts", requireContext().MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString("contactList", null);
        Type type = new TypeToken<ArrayList<Contact>>() {
        }.getType();
        List<Contact> loadedContacts = gson.fromJson(json, type);

        if (loadedContacts == null) {
            contactList = new ArrayList<>();
        } else {
            contactList = loadedContacts;

            if (customContactContainer != null) {
                for (Contact contact : contactList) {
                    LinearLayout contactView = createContactItem("📞", contact.getName(), contact.getPhone());
                    customContactContainer.addView(contactView);
                }
            }
        }
    }
}


