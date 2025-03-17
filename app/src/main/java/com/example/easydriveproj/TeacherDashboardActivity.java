package com.example.easydriveproj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TeacherDashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;
    private TextView teacherNameTextView;
    private TextView lessonCountTextView;
    private TextView ratingTextView;
    private ImageView teacherProfileImage;
    private MaterialCardView editProfileCard;
    private MaterialCardView scheduleCard;
    private MaterialCardView studentsCard;
    private MaterialCardView earningsCard;
    private FloatingActionButton addLessonFab;
    private RecyclerView upcomingLessonsRecyclerView;
    private View emptyLessonsView;
    private View instructorRegistrationCard;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

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

        currentUserId = currentUser.getUid();

        initializeViews();
        setupNavigationAndListeners();
        loadTeacherData();
        loadUpcomingLessons();
    }

    private void initializeViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        teacherNameTextView = findViewById(R.id.teacherNameTextView);
        lessonCountTextView = findViewById(R.id.lessonCountTextView);
        ratingTextView = findViewById(R.id.ratingTextView);
        teacherProfileImage = findViewById(R.id.teacherProfileImage);
        editProfileCard = findViewById(R.id.editProfileCard);
        scheduleCard = findViewById(R.id.scheduleCard);
        studentsCard = findViewById(R.id.studentsCard);
        earningsCard = findViewById(R.id.earningsCard);
        addLessonFab = findViewById(R.id.addLessonFab);
        upcomingLessonsRecyclerView = findViewById(R.id.upcomingLessonsRecyclerView);
        emptyLessonsView = findViewById(R.id.emptyLessonsView);
        instructorRegistrationCard = findViewById(R.id.instructorRegistrationCard);

        // Setup RecyclerView
        upcomingLessonsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupNavigationAndListeners() {
        // Bottom navigation
        bottomNavigation.setSelectedItemId(R.id.navigation_home);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    // Already on home
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    // Navigate to teacher profile
                    Intent intent = new Intent(TeacherDashboardActivity.this, TeacherProfileActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_favorites) {
                    // Navigate to teacher calendar/schedule
                    Intent intent = new Intent(TeacherDashboardActivity.this, TeacherScheduleActivity.class);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });

        // Card click listeners
        editProfileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherDashboardActivity.this, TeacherProfileActivity.class);
                startActivity(intent);
            }
        });

        scheduleCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherDashboardActivity.this, TeacherScheduleActivity.class);
                startActivity(intent);
            }
        });

        studentsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Check if current user has instructor ID first
                    String userId = mAuth.getCurrentUser().getUid();
                    mDatabase.child("Users").child(userId).child("instructorId")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists() && snapshot.getValue() != null) {
                                        // Found instructor ID, safe to navigate
                                        Intent intent = new Intent(TeacherDashboardActivity.this, TeacherStudentsActivity.class);
                                        // Pass instructor ID to the students activity
                                        intent.putExtra("INSTRUCTOR_ID", snapshot.getValue(String.class));
                                        startActivity(intent);
                                    } else {
                                        // No instructor ID found
                                        Toast.makeText(TeacherDashboardActivity.this,
                                                "אנא צור פרופיל מורה תחילה", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(TeacherDashboardActivity.this,
                                            "שגיאה בטעינת נתוני מורה", Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (Exception e) {
                    Toast.makeText(TeacherDashboardActivity.this,
                            "שגיאה במעבר לרשימת תלמידים: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    Log.e("TeacherDashboard", "Error navigating to students: " + e.getMessage(), e);
                }
            }
        });

        earningsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherDashboardActivity.this, TeacherEarningsActivity.class);
                startActivity(intent);
            }
        });

        // Add lesson button
        addLessonFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherDashboardActivity.this, AddLessonActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadTeacherData() {
        // Load basic user information
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getDisplayName() != null) {
            teacherNameTextView.setText(user.getDisplayName());

            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .circleCrop()
                        .into(teacherProfileImage);
            }
        }

        // First check if user has instructorId in their user data
        mDatabase.child("Users").child(currentUserId).child("instructorId")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.getValue(String.class) != null) {
                            // User has an instructor ID, hide registration card
                            instructorRegistrationCard.setVisibility(View.GONE);

                            // Load instructor data using that ID
                            String instructorId = dataSnapshot.getValue(String.class);
                            loadInstructorDataById(instructorId);
                        } else {
                            // Fallback to checking by userId
                            checkInstructorByUserId();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(TeacherDashboardActivity.this,
                                "שגיאה בטעינת נתונים: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // Load lessons count
        mDatabase.child("Lessons").orderByChild("instructorId").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        long lessonCount = dataSnapshot.getChildrenCount();
                        lessonCountTextView.setText(String.valueOf(lessonCount));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(TeacherDashboardActivity.this,
                                "שגיאה בטעינת נתוני שיעורים: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Add this helper method to load instructor data by ID
    private void loadInstructorDataById(String instructorId) {
        mDatabase.child("Instructors").child(instructorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Instructor instructor = dataSnapshot.getValue(Instructor.class);
                        if (instructor != null) {
                            updateUIWithInstructorData(instructor);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(TeacherDashboardActivity.this,
                                "שגיאה בטעינת נתונים: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Add this method to maintain backward compatibility
    private void checkInstructorByUserId() {
        // Check if user is already registered as an instructor
        mDatabase.child("Instructors").orderByChild("userId").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // User is already an instructor, hide registration card
                            instructorRegistrationCard.setVisibility(View.GONE);

                            // Load instructor data
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Instructor instructor = snapshot.getValue(Instructor.class);
                                if (instructor != null) {
                                    // Also store the instructor ID in user data for future reference
                                    mDatabase.child("Users").child(currentUserId).child("instructorId")
                                            .setValue(snapshot.getKey());

                                    updateUIWithInstructorData(instructor);
                                }
                            }
                        } else {
                            // User is not registered as an instructor yet
                            promptInstructorRegistration();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(TeacherDashboardActivity.this,
                                "שגיאה בטעינת נתונים: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUIWithInstructorData(Instructor instructor) {
        // Update UI elements with instructor data
        if (instructor.getName() != null && !instructor.getName().isEmpty()) {
            teacherNameTextView.setText(instructor.getName());
        }

        ratingTextView.setText(String.format("%.1f", instructor.getRating()));

        // Load instructor image if available
        if (instructor.getImageUrl() != null && !instructor.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(instructor.getImageUrl())
                    .circleCrop()
                    .into(teacherProfileImage);
        }
    }

    private void promptInstructorRegistration() {
        // Show registration card
        instructorRegistrationCard.setVisibility(View.VISIBLE);

        // Setup register button
        Button registerButton = findViewById(R.id.registerAsInstructorButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TeacherDashboardActivity.this, CreateInstructorProfileActivity.class));
            }
        });
    }

    private void loadUpcomingLessons() {
        // Get today's date as starting point
        long currentTime = System.currentTimeMillis();

        mDatabase.child("Lessons")
                .orderByChild("instructorId")
                .equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Lesson> upcomingLessons = new ArrayList<>();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Lesson lesson = snapshot.getValue(Lesson.class);
                            if (lesson != null && lesson.getStartTime() > currentTime) {
                                // Add future lessons to the list
                                lesson.setId(snapshot.getKey());
                                upcomingLessons.add(lesson);
                            }
                        }

                        // Sort by start time
                        upcomingLessons.sort((l1, l2) -> Long.compare(l1.getStartTime(), l2.getStartTime()));

                        // Create adapter with lessons
                        LessonAdapter adapter = new LessonAdapter(TeacherDashboardActivity.this, upcomingLessons);
                        upcomingLessonsRecyclerView.setAdapter(adapter);

                        // Show or hide empty view
                        if (upcomingLessons.isEmpty()) {
                            emptyLessonsView.setVisibility(View.VISIBLE);
                            upcomingLessonsRecyclerView.setVisibility(View.GONE);
                        } else {
                            emptyLessonsView.setVisibility(View.GONE);
                            upcomingLessonsRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(TeacherDashboardActivity.this,
                                "שגיאה בטעינת שיעורים: " + databaseError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}