package com.example.easydriveproj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateInstructorProfileActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText phoneEditText;
    private EditText cityEditText;
    private EditText priceEditText;
    private Spinner transmissionSpinner;
    private EditText carTypeEditText;
    private Button createProfileButton;
    private Button cancelButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_instructor_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Check if user is logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        initializeViews();
        setupListeners();

        // Pre-fill name and phone if available
        if (currentUser.getDisplayName() != null) {
            nameEditText.setText(currentUser.getDisplayName());
        }

        // You could get phone from Firebase user if it's stored
    }

    private void initializeViews() {
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        cityEditText = findViewById(R.id.cityEditText);
        priceEditText = findViewById(R.id.priceEditText);
        transmissionSpinner = findViewById(R.id.transmissionSpinner);
        carTypeEditText = findViewById(R.id.carTypeEditText);
        createProfileButton = findViewById(R.id.createProfileButton);
        cancelButton = findViewById(R.id.cancelButton);
    }

    private void setupListeners() {
        createProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createInstructorProfile();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void createInstructorProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();
        String priceText = priceEditText.getText().toString().trim();
        String transmissionType = transmissionSpinner.getSelectedItem().toString();
        String carType = carTypeEditText.getText().toString().trim();

        // Validate input
        if (name.isEmpty() || phone.isEmpty() || city.isEmpty() || priceText.isEmpty() || carType.isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        int price;
        try {
            price = Integer.parseInt(priceText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "מחיר לא תקין", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create instructor object
        Instructor instructor = new Instructor(
                userId,
                name,
                phone,
                city,
                "", // No image URL yet
                0.0f, // Default rating
                price,
                transmissionType,
                carType,
                true // Available by default
        );

        // Save to database
        String instructorId = mDatabase.child("Instructors").push().getKey();
        mDatabase.child("Instructors").child(instructorId).setValue(instructor)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CreateInstructorProfileActivity.this,
                                "פרופיל המורה נוצר בהצלחה", Toast.LENGTH_SHORT).show();

                        // Store the instructor ID in user data for easy reference
                        mDatabase.child("Users").child(userId).child("instructorId").setValue(instructorId);

                        // Navigate to TeacherProfileActivity instead of TeacherDashboardActivity
                        Intent intent = new Intent(CreateInstructorProfileActivity.this, TeacherProfileActivity.class);
                        // Pass instructor ID to the profile activity
                        intent.putExtra("INSTRUCTOR_ID", instructorId);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(CreateInstructorProfileActivity.this,
                                "שגיאה ביצירת פרופיל: " + task.getException(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}