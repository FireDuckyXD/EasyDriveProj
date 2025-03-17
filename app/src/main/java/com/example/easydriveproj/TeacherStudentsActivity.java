package com.example.easydriveproj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TeacherStudentsActivity extends AppCompatActivity {

    private static final String TAG = "TeacherStudentsActivity";

    private RecyclerView studentsRecyclerView;
    private List<Lesson> studentLessonsList;
    private BottomNavigationView bottomNavigation;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String instructorId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Starting activity");

        try {
            setContentView(R.layout.activity_teacher_students);

            // Set up toolbar
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("התלמידים שלי");
            }

            // Initialize Firebase
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance().getReference();

            if (mAuth.getCurrentUser() == null) {
                Toast.makeText(this, "משתמש לא מחובר", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return;
            }

            // Initialize views
            studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
            bottomNavigation = findViewById(R.id.bottomNavigation);

            // Check if we received an instructor ID from the intent
            if (getIntent().hasExtra("INSTRUCTOR_ID")) {
                instructorId = getIntent().getStringExtra("INSTRUCTOR_ID");
                Log.d(TAG, "Received instructor ID: " + instructorId);
            }

            // Setup RecyclerView
            studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            studentLessonsList = new ArrayList<>();

            // Set up the adapter with empty list first
            LessonAdapter adapter = new LessonAdapter(this, studentLessonsList);
            studentsRecyclerView.setAdapter(adapter);

            // Load students
            loadStudents();

            // Setup bottom navigation
            setupBottomNavigation();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "שגיאה באתחול מסך: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadStudents() {
        try {
            final String currentUserId = mAuth.getCurrentUser().getUid();

            // If we don't have instructor ID yet, try to get it from the database
            if (instructorId == null) {
                mDatabase.child("Users").child(currentUserId).child("instructorId")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    instructorId = snapshot.getValue(String.class);
                                    queryLessons(currentUserId);
                                } else {
                                    // Fallback to using userId directly
                                    queryLessons(currentUserId);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "Error getting instructor ID: " + error.getMessage());
                                // Fallback to using userId directly
                                queryLessons(currentUserId);
                            }
                        });
            } else {
                // We already have instructor ID from intent
                queryLessons(currentUserId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in loadStudents: " + e.getMessage(), e);
            Toast.makeText(this, "שגיאה בטעינת תלמידים: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void queryLessons(String instructorId) {
        // Query lessons for this instructor
        Query studentsQuery = mDatabase.child("Lessons")
                .orderByChild("instructorId")
                .equalTo(instructorId);

        studentsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentLessonsList.clear();

                // Collect unique lessons
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        Lesson lesson = snapshot.getValue(Lesson.class);
                        if (lesson != null) {
                            // Make sure to set the lesson ID
                            lesson.setId(snapshot.getKey());
                            studentLessonsList.add(lesson);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing lesson data: " + e.getMessage(), e);
                    }
                }

                // If no students found
                if (studentLessonsList.isEmpty()) {
                    Toast.makeText(TeacherStudentsActivity.this,
                            "אין תלמידים כרגע", Toast.LENGTH_SHORT).show();
                }

                // Create and set adapter
                LessonAdapter adapter = new LessonAdapter(TeacherStudentsActivity.this, studentLessonsList);
                studentsRecyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(TeacherStudentsActivity.this,
                        "שגיאה בטעינת תלמידים: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_favorites);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    startActivity(new Intent(TeacherStudentsActivity.this, TeacherDashboardActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    startActivity(new Intent(TeacherStudentsActivity.this, TeacherProfileActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_favorites) {
                    // Already in students view
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}