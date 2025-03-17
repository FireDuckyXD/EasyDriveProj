package com.example.easydriveproj;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddLessonActivity extends AppCompatActivity {

    private EditText studentNameEditText;
    private EditText studentPhoneEditText;
    private EditText startTimeEditText;
    private EditText endTimeEditText;
    private EditText locationEditText;
    private EditText notesEditText;
    private Button saveButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Calendar calendar;
    private long selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lesson);

        // Setup toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("הוספת שיעור חדש");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            finish();
            return;
        }

        // Get selected date from intent
        selectedDate = getIntent().getLongExtra("SELECTED_DATE", System.currentTimeMillis());
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDate);

        initializeViews();
        setupListeners();

        // Show selected date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        setTitle("הוספת שיעור ליום " + dateFormat.format(new Date(selectedDate)));
    }

    private void initializeViews() {
        studentNameEditText = findViewById(R.id.studentNameEditText);
        studentPhoneEditText = findViewById(R.id.studentPhoneEditText);
        startTimeEditText = findViewById(R.id.startTimeEditText);
        endTimeEditText = findViewById(R.id.endTimeEditText);
        locationEditText = findViewById(R.id.locationEditText);
        notesEditText = findViewById(R.id.notesEditText);
        saveButton = findViewById(R.id.saveButton);
    }

    private void setupListeners() {
        startTimeEditText.setOnClickListener(v -> showTimePickerDialog(true));
        endTimeEditText.setOnClickListener(v -> showTimePickerDialog(false));

        saveButton.setOnClickListener(v -> saveLesson());
    }

    private void showTimePickerDialog(final boolean isStartTime) {
        // Get current time as default
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Format time and set to EditText
                        String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        if (isStartTime) {
                            startTimeEditText.setText(time);
                            // Automatically set end time to start time + 1 hour
                            if (endTimeEditText.getText().toString().isEmpty()) {
                                String endTime = String.format(Locale.getDefault(), "%02d:%02d",
                                        (hourOfDay + 1) % 24, minute);
                                endTimeEditText.setText(endTime);
                            }
                        } else {
                            endTimeEditText.setText(time);
                        }
                    }
                },
                hour,
                minute,
                true);

        timePickerDialog.show();
    }

    private void saveLesson() {
        String studentName = studentNameEditText.getText().toString().trim();
        String studentPhone = studentPhoneEditText.getText().toString().trim();
        String startTimeText = startTimeEditText.getText().toString().trim();
        String endTimeText = endTimeEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String notes = notesEditText.getText().toString().trim();

        // Validate input
        if (studentName.isEmpty() || studentPhone.isEmpty() ||
                startTimeText.isEmpty() || endTimeText.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל השדות החובה", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse times
        String[] startTimeParts = startTimeText.split(":");
        String[] endTimeParts = endTimeText.split(":");

        if (startTimeParts.length != 2 || endTimeParts.length != 2) {
            Toast.makeText(this, "פורמט זמן לא תקין", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int startHour = Integer.parseInt(startTimeParts[0]);
            int startMinute = Integer.parseInt(startTimeParts[1]);
            int endHour = Integer.parseInt(endTimeParts[0]);
            int endMinute = Integer.parseInt(endTimeParts[1]);

            // Set calendar to selected date with start time
            Calendar startCalendar = (Calendar) calendar.clone();
            startCalendar.set(Calendar.HOUR_OF_DAY, startHour);
            startCalendar.set(Calendar.MINUTE, startMinute);
            startCalendar.set(Calendar.SECOND, 0);

            // Set calendar to selected date with end time
            Calendar endCalendar = (Calendar) calendar.clone();
            endCalendar.set(Calendar.HOUR_OF_DAY, endHour);
            endCalendar.set(Calendar.MINUTE, endMinute);
            endCalendar.set(Calendar.SECOND, 0);

            // Validate end time is after start time
            if (endCalendar.getTimeInMillis() <= startCalendar.getTimeInMillis()) {
                Toast.makeText(this, "זמן הסיום חייב להיות אחרי זמן ההתחלה", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get current user ID
            String instructorId = mAuth.getCurrentUser().getUid();

            // Create lesson object
            Lesson lesson = new Lesson(
                    instructorId,
                    "", // No studentId for now
                    studentName,
                    studentPhone,
                    startCalendar.getTimeInMillis(),
                    endCalendar.getTimeInMillis(),
                    location,
                    "scheduled", // Default status
                    notes,
                    false // Not paid yet
            );

            // Save to database
            DatabaseReference lessonsRef = mDatabase.child("Lessons");
            String lessonId = lessonsRef.push().getKey();

            lessonsRef.child(lessonId).setValue(lesson)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddLessonActivity.this,
                                    "השיעור נוסף בהצלחה", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(AddLessonActivity.this,
                                    "שגיאה בהוספת שיעור: " + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "פורמט זמן לא תקין", Toast.LENGTH_SHORT).show();
        }
    }
}