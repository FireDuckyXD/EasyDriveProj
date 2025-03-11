package com.example.easydriveproj;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {

    private MaterialButton personalInfoButton;
    private MaterialButton paymentMethodsButton;
    private MaterialButton historyButton;
    private MaterialButton settingsButton;
    private MaterialButton logoutButton;
    private BottomNavigationView bottomNavigation;

    // Profile views
    private ImageView profileImage;
    private TextView userName;
    private TextView userEmail;

    // Firebase references
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initializeViews();
        setupClickListeners();
        setupBottomNavigation();
        loadUserProfile();
    }

    private void initializeViews() {
        personalInfoButton = findViewById(R.id.personalInfoButton);
        paymentMethodsButton = findViewById(R.id.paymentMethodsButton);
        historyButton = findViewById(R.id.historyButton);
        settingsButton = findViewById(R.id.settingsButton);
        logoutButton = findViewById(R.id.logoutButton);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Initialize profile views
        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Set email
            userEmail.setText(currentUser.getEmail());

            // Set name and profile picture
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                userName.setText(currentUser.getDisplayName());
            }

            // Load profile picture
            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(currentUser.getPhotoUrl())
                        .placeholder(R.drawable.default_user_pic)
                        .error(R.drawable.default_user_pic)
                        .circleCrop()
                        .into(profileImage);
            }

            // Fetch additional user details from Realtime Database
            mDatabase.child("Users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Override name from database if available
                        String name = snapshot.child("name").getValue(String.class);
                        if (name != null && !name.isEmpty()) {
                            userName.setText(name);
                        } else if (currentUser.getDisplayName() == null || currentUser.getDisplayName().isEmpty()) {
                            // Fallback to email username if no name is set
                            userName.setText(currentUser.getEmail().split("@")[0]);
                        }

                        // Override profile picture from database if available
                        String profilePicUrl = snapshot.child("profilePicUrl").getValue(String.class);
                        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                            Glide.with(ProfileActivity.this)
                                    .load(profilePicUrl)
                                    .placeholder(R.drawable.default_user_pic)
                                    .error(R.drawable.default_user_pic)
                                    .circleCrop()
                                    .into(profileImage);
                        }
                    } else {
                        // No additional user data found
                        if (currentUser.getDisplayName() == null || currentUser.getDisplayName().isEmpty()) {
                            userName.setText(currentUser.getEmail().split("@")[0]);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ProfileActivity.this,
                            "שגיאה בטעינת פרופיל: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // No user logged in - redirect to login
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void setupBottomNavigation() {
        // Set the selected item to profile
        bottomNavigation.setSelectedItemId(R.id.navigation_profile);

        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    // Navigate to Home
                    Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish(); // Close this activity
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    // Already in Profile
                    return true;
                }
                return false;
            }
        });
    }

    private void setupClickListeners() {
        personalInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "מעבר לפרטים אישיים", Toast.LENGTH_SHORT).show();
            }
        });

        paymentMethodsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "מעבר לאמצעי תשלום", Toast.LENGTH_SHORT).show();
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "מעבר להיסטוריית שיעורים", Toast.LENGTH_SHORT).show();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "מעבר להגדרות", Toast.LENGTH_SHORT).show();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out from Firebase
                mAuth.signOut();

                // Redirect to login screen
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();  // Call the super method first
        // Navigate back to Home when back button is pressed
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}