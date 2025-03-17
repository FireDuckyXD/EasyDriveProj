package com.example.easydriveproj;

import android.content.Intent;
import android.os.Bundle;
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

    private RecyclerView studentsRecyclerView;
    private List<Lesson> studentLessonsList;
    private BottomNavigationView bottomNavigation;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        // Initialize views
        studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Setup RecyclerView
        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentLessonsList = new ArrayList<>();

        // Load students
        loadStudents();

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void loadStudents() {
        String currentInstructorId = mAuth.getCurrentUser().getUid();

        // Query lessons for this instructor
        Query studentsQuery = mDatabase.child("Lessons")
                .orderByChild("instructorId")
                .equalTo(currentInstructorId);

        studentsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentLessonsList.clear();

                // Collect unique lessons
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Lesson lesson = snapshot.getValue(Lesson.class);
                    if (lesson != null) {
                        studentLessonsList.add(lesson);
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