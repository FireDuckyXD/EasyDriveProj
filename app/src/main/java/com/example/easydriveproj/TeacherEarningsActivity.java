package com.example.easydriveproj;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
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
import java.util.Locale;

public class TeacherEarningsActivity extends AppCompatActivity {

    private RecyclerView earningsRecyclerView;
    private TextView totalEarningsTextView;
    private TextView totalLessonsTextView;
    private List<Lesson> paidLessonsList;
    private BottomNavigationView bottomNavigation;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_earnings);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("הכנסות");
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        earningsRecyclerView = findViewById(R.id.earningsRecyclerView);
        totalEarningsTextView = findViewById(R.id.totalEarningsTextView);
        totalLessonsTextView = findViewById(R.id.totalLessonsTextView);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Setup RecyclerView
        earningsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        paidLessonsList = new ArrayList<>();

        // Load earnings data
        loadEarningsData();

        // Setup bottom navigation
        setupBottomNavigation();
    }

    private void loadEarningsData() {
        String currentInstructorId = mAuth.getCurrentUser().getUid();

        // Query paid lessons for this instructor
        Query earningsQuery = mDatabase.child("Lessons")
                .orderByChild("instructorId")
                .equalTo(currentInstructorId);

        earningsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                paidLessonsList.clear();
                int totalEarnings = 0;
                int totalLessons = 0;

                // Collect paid lessons
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Lesson lesson = snapshot.getValue(Lesson.class);
                    if (lesson != null && lesson.isPaid()) {
                        paidLessonsList.add(lesson);
                        totalEarnings += 180; // Assuming fixed price of 180 NIS per lesson
                        totalLessons++;
                    }
                }

                // Update UI
                totalEarningsTextView.setText(String.format(Locale.getDefault(), "%d ₪", totalEarnings));
                totalLessonsTextView.setText(String.format(Locale.getDefault(), "%d שיעורים", totalLessons));

                // If no paid lessons found
                if (paidLessonsList.isEmpty()) {
                    Toast.makeText(TeacherEarningsActivity.this,
                            "אין שיעורים שולמו עדיין", Toast.LENGTH_SHORT).show();
                }

                // Create and set adapter
                LessonAdapter adapter = new LessonAdapter(TeacherEarningsActivity.this, paidLessonsList);
                earningsRecyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TeacherEarningsActivity.this,
                        "שגיאה בטעינת הכנסות: " + databaseError.getMessage(),
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
                    startActivity(new Intent(TeacherEarningsActivity.this, TeacherDashboardActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    startActivity(new Intent(TeacherEarningsActivity.this, TeacherProfileActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_favorites) {
                    // Already in earnings view
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