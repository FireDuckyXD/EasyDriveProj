package com.example.easydriveproj;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TeacherScheduleActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private RecyclerView lessonsRecyclerView;
    private TextView emptyStateTextView;
    private FloatingActionButton addLessonFab;
    private BottomNavigationView bottomNavigation;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String currentUserId;
    private long selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_schedule);

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

        // Initialize views
        initializeViews();
        setupListeners();
        setupBottomNavigation();

        // Set default date to today
        Calendar calendar = Calendar.getInstance();
        selectedDate = calendar.getTimeInMillis();

        // Load lessons for today
        loadLessonsForDate(selectedDate);
    }

    private void initializeViews() {
        calendarView = findViewById(R.id.calendarView);
        lessonsRecyclerView = findViewById(R.id.lessonsRecyclerView);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);
        addLessonFab = findViewById(R.id.addLessonFab);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Setup RecyclerView
        lessonsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupListeners() {
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                selectedDate = calendar.getTimeInMillis();

                loadLessonsForDate(selectedDate);
            }
        });

        addLessonFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherScheduleActivity.this, AddLessonActivity.class);
                intent.putExtra("SELECTED_DATE", selectedDate);
                startActivity(intent);
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
                    startActivity(new Intent(TeacherScheduleActivity.this, TeacherDashboardActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    startActivity(new Intent(TeacherScheduleActivity.this, TeacherProfileActivity.class));
                    return true;
                } else if (itemId == R.id.nav_favorites) {
                    // Already in schedule
                    return true;
                }
                return false;
            }
        });
    }

    private void loadLessonsForDate(long date) {
        // Calculate start and end of the selected day
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDay = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        long endOfDay = calendar.getTimeInMillis();

        // Query lessons for this instructor on the selected date
        Query query = mDatabase.child("Lessons")
                .orderByChild("instructorId")
                .equalTo(currentUserId);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Lesson> dayLessons = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Lesson lesson = snapshot.getValue(Lesson.class);
                    if (lesson != null) {
                        // Only include lessons on the selected date
                        if (lesson.getStartTime() >= startOfDay && lesson.getStartTime() <= endOfDay) {
                            lesson.setId(snapshot.getKey());
                            dayLessons.add(lesson);
                        }
                    }
                }

                // Sort lessons by start time
                dayLessons.sort((l1, l2) -> Long.compare(l1.getStartTime(), l2.getStartTime()));

                // Update UI
                if (dayLessons.isEmpty()) {
                    emptyStateTextView.setVisibility(View.VISIBLE);
                    lessonsRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyStateTextView.setVisibility(View.GONE);
                    lessonsRecyclerView.setVisibility(View.VISIBLE);

                    LessonAdapter adapter = new LessonAdapter(TeacherScheduleActivity.this, dayLessons);
                    lessonsRecyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TeacherScheduleActivity.this,
                        "שגיאה בטעינת שיעורים: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload lessons when returning to this activity
        loadLessonsForDate(selectedDate);
    }
}