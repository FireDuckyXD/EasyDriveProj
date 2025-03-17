package com.example.easydriveproj;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LessonDetailActivity extends AppCompatActivity {

    private TextView studentNameTextView;
    private TextView dateTextView;
    private TextView timeTextView;
    private TextView locationTextView;
    private TextView notesTextView;
    private TextView statusTextView;
    private TextView paymentStatusTextView;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_detail);

        // Setup toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("פרטי שיעור");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Initialize Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize views
        initializeViews();

        // Get lesson ID from intent
        String lessonId = getIntent().getStringExtra("LESSON_ID");
        if (lessonId != null) {
            loadLessonDetails(lessonId);
        } else {
            Toast.makeText(this, "שגיאה: מזהה שיעור חסר", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        studentNameTextView = findViewById(R.id.studentNameTextView);
        dateTextView = findViewById(R.id.dateTextView);
        timeTextView = findViewById(R.id.timeTextView);
        locationTextView = findViewById(R.id.locationTextView);
        notesTextView = findViewById(R.id.notesTextView);
        statusTextView = findViewById(R.id.statusTextView);
        paymentStatusTextView = findViewById(R.id.paymentStatusTextView);
    }

    private void loadLessonDetails(String lessonId) {
        mDatabase.child("Lessons").child(lessonId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Lesson lesson = snapshot.getValue(Lesson.class);
                if (lesson != null) {
                    // Populate views with lesson details
                    studentNameTextView.setText(lesson.getStudentName());
                    dateTextView.setText(lesson.getFormattedDate());
                    timeTextView.setText(lesson.getFormattedStartTime() + " - " + lesson.getFormattedEndTime());
                    locationTextView.setText(lesson.getLocation());
                    notesTextView.setText(lesson.getNotes() != null ? lesson.getNotes() : "אין הערות");
                    statusTextView.setText(getStatusText(lesson.getStatus()));
                    paymentStatusTextView.setText(lesson.isPaid() ? "שולם" : "לא שולם");
                } else {
                    Toast.makeText(LessonDetailActivity.this, "שגיאה: לא ניתן לטעון את פרטי השיעור", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LessonDetailActivity.this, "שגיאה: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private String getStatusText(String status) {
        switch (status) {
            case "scheduled":
                return "מתוכנן";
            case "completed":
                return "הושלם";
            case "cancelled":
                return "בוטל";
            default:
                return "סטטוס לא ידוע";
        }
    }
}