package com.example.easydriveproj;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TeacherProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImageView;
    private EditText nameEditText;
    private EditText phoneEditText;
    private EditText cityEditText;
    private EditText priceEditText;
    private Spinner transmissionSpinner;
    private EditText carTypeEditText;
    private Switch availabilitySwitch;
    private Button saveButton;
    private Button logoutButton;
    private BottomNavigationView bottomNavigation;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Uri imageUri;
    private String currentInstructorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Not logged in, redirect to login
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        initializeViews();
        setupListeners();
        setupBottomNavigation();
        loadInstructorData();
    }

    private void initializeViews() {
        profileImageView = findViewById(R.id.profileImageView);
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        cityEditText = findViewById(R.id.cityEditText);
        priceEditText = findViewById(R.id.priceEditText);
        transmissionSpinner = findViewById(R.id.transmissionSpinner);
        carTypeEditText = findViewById(R.id.carTypeEditText);
        availabilitySwitch = findViewById(R.id.availabilitySwitch);
        saveButton = findViewById(R.id.saveButton);
        logoutButton = findViewById(R.id.logoutButton);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupListeners() {
        profileImageView.setOnClickListener(v -> {
            openFileChooser();
        });

        saveButton.setOnClickListener(v -> {
            saveInstructorData();
        });

        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(TeacherProfileActivity.this, MainActivity.class));
            finish();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.navigation_profile);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    startActivity(new Intent(TeacherProfileActivity.this, TeacherDashboardActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    // Already in profile
                    return true;
                } else if (itemId == R.id.nav_favorites) {
                    startActivity(new Intent(TeacherProfileActivity.this, TeacherScheduleActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).circleCrop().into(profileImageView);
        }
    }

    private void loadInstructorData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userId = currentUser.getUid();

        // First load basic user info
        if (currentUser.getDisplayName() != null) {
            nameEditText.setText(currentUser.getDisplayName());
        }
        if (currentUser.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .circleCrop()
                    .into(profileImageView);
        }

        // Then check if user is already registered as an instructor
        mDatabase.child("Instructors").orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // User is already an instructor, load instructor data
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                currentInstructorId = snapshot.getKey();
                                Instructor instructor = snapshot.getValue(Instructor.class);
                                if (instructor != null) {
                                    fillInstructorFields(instructor);
                                }
                            }
                        } else {
                            // User is not registered as an instructor yet
                            // Keep fields empty or with default values
                            Toast.makeText(TeacherProfileActivity.this,
                                    "לא נמצא פרופיל מורה. אנא מלא את הפרטים ושמור",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(TeacherProfileActivity.this,
                                "שגיאה בטעינת נתונים: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fillInstructorFields(Instructor instructor) {
        nameEditText.setText(instructor.getName());
        phoneEditText.setText(instructor.getPhoneNumber());
        cityEditText.setText(instructor.getCity());
        priceEditText.setText(String.valueOf(instructor.getPricePerLesson()));

        // Set transmission type
        String transmissionType = instructor.getTransmissionType();
        if (transmissionType != null) {
            if (transmissionType.equals("אוטומט")) {
                transmissionSpinner.setSelection(0);
            } else if (transmissionType.equals("ידני")) {
                transmissionSpinner.setSelection(1);
            }
        }

        carTypeEditText.setText(instructor.getCarType());
        availabilitySwitch.setChecked(instructor.isAvailable());

        if (instructor.getImageUrl() != null && !instructor.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(instructor.getImageUrl())
                    .circleCrop()
                    .into(profileImageView);
        }
    }

    private void saveInstructorData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String city = cityEditText.getText().toString().trim();
        String priceText = priceEditText.getText().toString().trim();
        String transmissionType = transmissionSpinner.getSelectedItem().toString();
        String carType = carTypeEditText.getText().toString().trim();
        boolean isAvailable = availabilitySwitch.isChecked();

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
                "", // Image URL will be updated later if needed
                0.0f, // Default rating
                price,
                transmissionType,
                carType,
                isAvailable
        );

        // Save to database
        DatabaseReference instructorsRef = mDatabase.child("Instructors");

        if (currentInstructorId != null) {
            // Update existing instructor
            instructorsRef.child(currentInstructorId).setValue(instructor)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(TeacherProfileActivity.this,
                                    "פרופיל המורה עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TeacherProfileActivity.this,
                                    "שגיאה בעדכון פרופיל: " + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Create new instructor
            String newInstructorId = instructorsRef.push().getKey();
            instructorsRef.child(newInstructorId).setValue(instructor)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            currentInstructorId = newInstructorId;
                            Toast.makeText(TeacherProfileActivity.this,
                                    "פרופיל המורה נוצר בהצלחה", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TeacherProfileActivity.this,
                                    "שגיאה ביצירת פרופיל: " + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}