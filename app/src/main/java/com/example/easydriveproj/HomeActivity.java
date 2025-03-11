package com.example.easydriveproj;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";

    private RecyclerView recyclerView;
    private InstructorAdapter adapter;
    private List<Instructor> instructorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Show that we're starting
        Toast.makeText(this, "HomeActivity started", Toast.LENGTH_SHORT).show();

        // Initialize the RecyclerView
        recyclerView = findViewById(R.id.instructorsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the instructor list
        instructorList = new ArrayList<>();

        // Create some test instructors directly
        instructorList.add(new Instructor(
                "1",
                "Test Instructor 1",
                "123456789",
                "Test City",
                "",
                4.5f,
                150,
                "אוטומט",
                "יונדאי",
                true
        ));

        instructorList.add(new Instructor(
                "2",
                "Test Instructor 2",
                "987654321",
                "Another City",
                "",
                3.8f,
                180,
                "ידני",
                "טויוטה",
                true
        ));

        // Initialize the adapter with test data
        adapter = new InstructorAdapter(this, instructorList);
        recyclerView.setAdapter(adapter);

        // Show that we've set up the adapter
        Toast.makeText(this, "Added " + instructorList.size() + " test instructors", Toast.LENGTH_SHORT).show();

        // Now try to load data from Firebase
        loadInstructorsFromFirebase();
    }

    private void loadInstructorsFromFirebase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Instructors");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Count the instructors
                long count = dataSnapshot.getChildrenCount();

                if (count > 0) {
                    // Clear the test data
                    instructorList.clear();

                    // Add the real instructors
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Instructor instructor = snapshot.getValue(Instructor.class);
                        if (instructor != null) {
                            instructor.setId(snapshot.getKey());
                            instructorList.add(instructor);
                        }
                    }

                    // Update the UI
                    adapter.notifyDataSetChanged();

                    // Show success message
                    Toast.makeText(HomeActivity.this,
                            "Loaded " + instructorList.size() + " instructors from Firebase",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // No data in Firebase, add some
                    addSampleDataToFirebase();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this,
                        "Error loading data: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addSampleDataToFirebase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Instructors");

        // Create sample instructors
        List<Instructor> sampleInstructors = new ArrayList<>();

        sampleInstructors.add(new Instructor(
                null,
                "אבי כהן",
                "0501234567",
                "תל אביב",
                "",
                4.7f,
                180,
                "אוטומט",
                "יונדאי i20",
                true
        ));

        sampleInstructors.add(new Instructor(
                null,
                "מיכל לוי",
                "0509876543",
                "חיפה",
                "",
                4.9f,
                200,
                "אוטומט",
                "טויוטה יאריס",
                true
        ));

        sampleInstructors.add(new Instructor(
                null,
                "יוסי גולן",
                "0527654321",
                "ירושלים",
                "",
                4.5f,
                160,
                "ידני",
                "מאזדה 2",
                true
        ));

        // Add them to Firebase
        for (int i = 0; i < sampleInstructors.size(); i++) {
            String key = "instructor-" + i;
            databaseRef.child(key).setValue(sampleInstructors.get(i));
        }

        Toast.makeText(this, "Added sample data to Firebase", Toast.LENGTH_SHORT).show();
    }
}