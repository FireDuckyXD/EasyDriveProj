package com.example.easydriveproj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    GoogleSignInClient googleSignInClient;
    ShapeableImageView imageView;
    TextView name, mail;

    // Define User class for database storage
    public static class User {
        private String userId;
        private String displayName;
        private String email;
        private String photoUrl;

        // Required empty constructor for Firebase
        public User() {
        }

        public User(String userId, String displayName, String email, String photoUrl) {
            this.userId = userId;
            this.displayName = displayName;
            this.email = email;
            this.photoUrl = photoUrl;
        }

        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhotoUrl() { return photoUrl; }
        public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.e("onActivityResult", result.toString());
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount signInAccount = accountTask.getResult(ApiException.class);
                            AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);

                            auth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        auth = FirebaseAuth.getInstance();
                                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                                        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid();
                                        final String userEmail = auth.getCurrentUser().getEmail();

                                        // Check if this email has been used before with a different account
                                        database.getReference("EmailToUser").child(encodeEmail(userEmail))
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        String previousUserId = snapshot.getValue(String.class);

                                                        if (previousUserId != null && !previousUserId.equals(userId)) {
                                                            // Email exists but linked to different account - transfer data
                                                            transferUserData(previousUserId, userId, userEmail);
                                                        } else {
                                                            // New user or same user, just save/update basic info
                                                            saveUserInfo(userId, userEmail);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Log.e("Firebase", "Error checking email: " + error.getMessage());
                                                        // Continue with account creation anyway
                                                        saveUserInfo(userId, userEmail);
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(MainActivity.this, "התחברות נכשלה" + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } catch (ApiException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        imageView = findViewById(R.id.profileImage);
        name = findViewById(R.id.nameTV);
        mail = findViewById(R.id.mailTV);

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(MainActivity.this, options);
        auth = FirebaseAuth.getInstance();

        SignInButton signInButton = findViewById(R.id.signIn);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Sign out first to force account selection
                googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // After signing out, launch the sign-in intent
                        Intent intent = googleSignInClient.getSignInIntent();
                        activityResultLauncher.launch(intent);
                    }
                });
            }
        });
    }

    // Helper method to save user info
    private void saveUserInfo(String userId, String email) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Create a custom User object
        User user = new User(
                userId,
                auth.getCurrentUser().getDisplayName(),
                email,
                auth.getCurrentUser().getPhotoUrl() != null ?
                        auth.getCurrentUser().getPhotoUrl().toString() : ""
        );

        // Save user info to Firebase Realtime Database
        database.getReference("Users").child(userId).setValue(user);

        // Create/update email to user mapping
        database.getReference("EmailToUser").child(encodeEmail(email)).setValue(userId);

        // Load profile data for UI
        Glide.with(MainActivity.this)
                .load(Objects.requireNonNull(auth.getCurrentUser()).getPhotoUrl())
                .into(imageView);

        name.setText(auth.getCurrentUser().getDisplayName());
        mail.setText(email);

        Toast.makeText(MainActivity.this, "התחברות בוצעה בהצלחה", Toast.LENGTH_LONG).show();

        // Navigate to role selection
        Intent intent = new Intent(MainActivity.this, RoleSelectionActivity.class);
        startActivity(intent);
        finish();
    }

    // Helper method to transfer user data from one account to another
    private void transferUserData(String oldUserId, final String newUserId, final String email) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference oldUserRef = database.getReference("Users").child(oldUserId);
        final DatabaseReference newUserRef = database.getReference("Users").child(newUserId);

        // First, check if the old user has instructor data to transfer
        database.getReference("Users").child(oldUserId).child("instructorId")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String instructorId = snapshot.getValue(String.class);

                        if (instructorId != null) {
                            // Transfer instructor ID to new user
                            newUserRef.child("instructorId").setValue(instructorId);

                            // Update the userId field in the instructor record
                            database.getReference("Instructors").child(instructorId).child("userId")
                                    .setValue(newUserId);

                            Toast.makeText(MainActivity.this,
                                    "פרופיל המורה שלך הועבר לחשבון הנוכחי",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // Create/update basic user info
                        User user = new User(
                                newUserId,
                                auth.getCurrentUser().getDisplayName(),
                                email,
                                auth.getCurrentUser().getPhotoUrl() != null ?
                                        auth.getCurrentUser().getPhotoUrl().toString() : ""
                        );

                        // Copy over role if exists
                        oldUserRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String role = snapshot.getValue(String.class);
                                    newUserRef.child("role").setValue(role);
                                }

                                // Save the new user info
                                newUserRef.setValue(user);

                                // Update email mapping
                                database.getReference("EmailToUser").child(encodeEmail(email)).setValue(newUserId);

                                // Load profile data for UI
                                Glide.with(MainActivity.this)
                                        .load(Objects.requireNonNull(auth.getCurrentUser()).getPhotoUrl())
                                        .into(imageView);

                                name.setText(auth.getCurrentUser().getDisplayName());
                                mail.setText(email);

                                Toast.makeText(MainActivity.this, "התחברות בוצעה בהצלחה", Toast.LENGTH_LONG).show();

                                // Navigate to role selection
                                Intent intent = new Intent(MainActivity.this, RoleSelectionActivity.class);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                // Proceed anyway with user creation
                                saveUserInfo(newUserId, email);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Proceed anyway with user creation
                        saveUserInfo(newUserId, email);
                    }
                });
    }

    // Helper method to encode email for Firebase path (cannot contain ., #, $, [, ])
    private String encodeEmail(String email) {
        return email.replace(".", ",").replace("#", "-")
                .replace("$", "_").replace("[", "(").replace("]", ")");
    }
}