package com.example.attnd;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RoleSelectionActivity extends AppCompatActivity {
    private Button btnSaveProfile;
    private EditText etName;
    private EditText etPrincipalId;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.prefs = getSharedPreferences("AttndPrefs", 0);
        if (this.prefs.contains("userRole")) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_role_selection);
        this.etName = findViewById(R.id.etName);
        this.etPrincipalId = findViewById(R.id.etPrincipalId);
        this.btnSaveProfile = findViewById(R.id.btnSaveProfile);

        this.btnSaveProfile.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String name = this.etName.getText().toString().trim();
        String principalIdInput = this.etPrincipalId.getText().toString().trim();
        
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (principalIdInput.isEmpty()) {
            Toast.makeText(this, "Teacher must enter Principal ID", Toast.LENGTH_SHORT).show();
            return;
        }
        
        this.btnSaveProfile.setEnabled(false);
        this.btnSaveProfile.setText("Validating Principal ID...");

        try {
            FirebaseDatabase.getInstance().getReference("Users").child("PRINCIPAL").child(principalIdInput)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                proceedWithSave(name, principalIdInput);
                            } else {
                                Toast.makeText(RoleSelectionActivity.this, "Invalid Principal ID! Please check and try again.", Toast.LENGTH_LONG).show();
                                btnSaveProfile.setEnabled(true);
                                btnSaveProfile.setText("Save & Continue");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(RoleSelectionActivity.this, "Validation Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            btnSaveProfile.setEnabled(true);
                            btnSaveProfile.setText("Save & Continue");
                        }
                    });
        } catch (Exception e) {
            // Firebase not initialized, fallback to local save
            proceedWithSave(name, principalIdInput);
        }
    }

    private void proceedWithSave(String name, String principalIdInput) {
        this.btnSaveProfile.setText("Saving Profile...");
        final String userId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String role = "TEACHER";
        
        SharedPreferences.Editor editor = this.prefs.edit();
        editor.putString("userId", userId);
        editor.putString("userRole", role);
        editor.putString("userName", name);
        editor.putString("principalId", principalIdInput);
        editor.apply();
        
        try {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users").child(role).child(userId);
            Map<String, String> profileData = new HashMap<>();
            profileData.put("name", name);
            profileData.put("principalId", principalIdInput);
            
            usersRef.setValue(profileData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Profile Saved! Your ID: " + userId, Toast.LENGTH_LONG).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, "Network Error: " + (task.getException() != null ? task.getException().getMessage() : "unknown error"), Toast.LENGTH_LONG).show();
                    this.prefs.edit().clear().apply();
                    this.btnSaveProfile.setEnabled(true);
                    this.btnSaveProfile.setText("Save & Continue");
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Firebase not initialized. Data saved locally.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}
