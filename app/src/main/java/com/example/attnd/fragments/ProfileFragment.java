package com.example.attnd.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.attnd.R;
import com.example.attnd.viewmodel.MainViewModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {
    private Button btnSaveProfile;
    private MaterialCardView cardMyIdCapsule;
    private TextInputEditText etPrincipalId;
    private TextInputEditText etProfileName;
    private TextInputLayout layoutPrincipalId;
    private SharedPreferences prefs;
    private TextView tvMyId;
    private TextView tvPrincipalName;
    private MainViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        this.viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        this.prefs = requireContext().getSharedPreferences("AttndPrefs", Context.MODE_PRIVATE);
        this.etProfileName = view.findViewById(R.id.etProfileName);
        this.etPrincipalId = view.findViewById(R.id.etPrincipalId);
        this.layoutPrincipalId = view.findViewById(R.id.layoutPrincipalId);
        this.tvMyId = view.findViewById(R.id.tvMyId);
        this.tvPrincipalName = view.findViewById(R.id.tvPrincipalName);
        this.cardMyIdCapsule = view.findViewById(R.id.cardMyIdCapsule);
        this.btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        
        view.findViewById(R.id.backButton).setOnClickListener(v -> 
            Navigation.findNavController(v).popBackStack()
        );

        loadProfileData();

        this.cardMyIdCapsule.setOnClickListener(v -> {
            String userId = this.prefs.getString("userId", "");
            if (!userId.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("User ID", userId);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getContext(), "ID Copied to Clipboard!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        this.btnSaveProfile.setOnClickListener(v -> saveProfileData());

        return view;
    }

    private void loadProfileData() {
        String name = this.prefs.getString("userName", "");
        String userId = this.prefs.getString("userId", "");
        String principalId = this.prefs.getString("principalId", "");
        this.etProfileName.setText(name);
        this.tvMyId.setText("ID: " + userId);
        this.layoutPrincipalId.setVisibility(View.VISIBLE);
        this.etPrincipalId.setText(principalId);
        if (!principalId.isEmpty()) {
            fetchPrincipalName(principalId);
        }
        this.etProfileName.setImeOptions(5);
    }

    private void fetchPrincipalName(String principalId) {
        this.tvPrincipalName.setVisibility(View.VISIBLE);
        this.tvPrincipalName.setText("My Principal: Loading...");
        FirebaseDatabase.getInstance().getReference("Users").child("PRINCIPAL").child(principalId).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String pName = snapshot.getValue(String.class);
                            tvPrincipalName.setText("My Principal: " + pName);
                        } else {
                            tvPrincipalName.setText("My Principal: Not Found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        tvPrincipalName.setText("My Principal: Error");
                    }
                });
    }

    private void saveProfileData() {
        this.etProfileName.clearFocus();
        if (this.etPrincipalId.getVisibility() == View.VISIBLE) {
            this.etPrincipalId.clearFocus();
        }
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getView() != null) {
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
        String newName = this.etProfileName.getText() != null ? this.etProfileName.getText().toString().trim() : "";
        String newPrincipalId = this.etPrincipalId.getText() != null ? this.etPrincipalId.getText().toString().trim() : "";
        if (newName.isEmpty()) {
            Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        this.btnSaveProfile.setEnabled(false);
        this.btnSaveProfile.setText("Validating...");

        try {
            FirebaseDatabase.getInstance().getReference("Users").child("PRINCIPAL").child(newPrincipalId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                proceedWithSave(newName, newPrincipalId);
                            } else {
                                Toast.makeText(getContext(), "Invalid Principal ID!", Toast.LENGTH_LONG).show();
                                btnSaveProfile.setEnabled(true);
                                btnSaveProfile.setText("Save Profile");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(getContext(), "Validation Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            btnSaveProfile.setEnabled(true);
                            btnSaveProfile.setText("Save Profile");
                        }
                    });
        } catch (Exception e) {
            proceedWithSave(newName, newPrincipalId);
        }
    }

    private void proceedWithSave(String newName, String newPrincipalId) {
        String role = "TEACHER";
        String userId = this.prefs.getString("userId", "");
        SharedPreferences.Editor editor = this.prefs.edit();
        editor.putString("userName", newName);
        editor.putString("principalId", newPrincipalId);
        editor.apply();
        fetchPrincipalName(newPrincipalId);
        if (!userId.isEmpty()) {
            try {
                FirebaseDatabase.getInstance().getReference("Users").child(role).child(userId).child("name").setValue(newName);
                FirebaseDatabase.getInstance().getReference("Users").child(role).child(userId).child("principalId").setValue(newPrincipalId);
            } catch (Exception e) {
                // Firebase not initialized
            }
        }
        this.btnSaveProfile.setEnabled(true);
        this.btnSaveProfile.setText("Save Profile");
        Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show();
    }
}
