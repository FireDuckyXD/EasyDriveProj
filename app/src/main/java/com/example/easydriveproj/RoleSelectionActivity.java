package com.example.easydriveproj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RoleSelectionActivity extends AppCompatActivity {

    private Button studentButton;
    private Button teacherButton;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        studentButton = findViewById(R.id.studentButton);
        teacherButton = findViewById(R.id.teacherButton);

        // Check if user already has a role
        checkExistingRole();

        studentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserRole("student");
            }
        });

        teacherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserRole("teacher");
            }
        });
    }

    private void checkExistingRole() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            mDatabase.child("Users").child(userId).child("role").addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String role = dataSnapshot.getValue(String.class);
                                navigateBasedOnRole(role);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(RoleSelectionActivity.this,
                                    "שגיאה בטעינת נתונים: " + databaseError.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveUserRole(String role) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            mDatabase.child("Users").child(userId).child("role").setValue(role)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            navigateBasedOnRole(role);
                        } else {
                            Toast.makeText(RoleSelectionActivity.this,
                                    "שגיאה בשמירת תפקיד: " + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void navigateBasedOnRole(String role) {
        Intent intent;
        if ("teacher".equals(role)) {
            intent = new Intent(RoleSelectionActivity.this, TeacherDashboardActivity.class);
        } else {
            // Default to student view
            intent = new Intent(RoleSelectionActivity.this, HomeActivity.class);
        }
        startActivity(intent);
        finish();
    }
}