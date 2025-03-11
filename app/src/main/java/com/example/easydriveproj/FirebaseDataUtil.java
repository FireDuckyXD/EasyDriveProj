package com.example.easydriveproj;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDataUtil {
    private static final String TAG = "FirebaseDataUtil";

    public static void populateSampleData(Context context) {
        DatabaseReference instructorsRef = FirebaseDatabase.getInstance().getReference("Instructors");

        Log.d(TAG, "Checking if sample data needs to be populated");
        Toast.makeText(context, "Checking if we need to add instructors...", Toast.LENGTH_SHORT).show();

        // Check if data already exists
        instructorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                Log.d(TAG, "Found " + count + " existing instructors");

                if (count == 0) {
                    Log.d(TAG, "No existing data found, adding sample instructors");
                    Toast.makeText(context, "Adding sample instructors...", Toast.LENGTH_SHORT).show();

                    // Hard-coded for testing - directly add one instructor
                    Instructor testInstructor = new Instructor(
                            "test-id",
                            "אבי כהן - TEST",
                            "0501234567",
                            "תל אביב",
                            "",
                            4.7f,
                            180,
                            "אוטומט",
                            "יונדאי i20",
                            true
                    );

                    instructorsRef.child("test-instructor").setValue(testInstructor)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Added test instructor successfully");
                                    Toast.makeText(context, "נוסף מורה לדוגמה", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.e(TAG, "Failed to add test instructor: " + task.getException());
                                    Toast.makeText(context, "שגיאה בהוספת מורה: " + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            });

                    // Add additional sample instructors
                    List<Instructor> instructors = getSampleInstructors();
                    for (int i = 0; i < instructors.size(); i++) {
                        Instructor instructor = instructors.get(i);
                        String key = "instructor-" + i;

                        instructorsRef.child(key).setValue(instructor)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Successfully added instructor: " + instructor.getName());
                                    } else {
                                        Log.e(TAG, "Failed to add instructor: " + instructor.getName());
                                    }
                                });
                    }
                } else {
                    Log.d(TAG, "Data already exists (" + count + " instructors), skipping sample data population");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(context, "שגיאה: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static List<Instructor> getSampleInstructors() {
        List<Instructor> instructors = new ArrayList<>();

        instructors.add(new Instructor(
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

        instructors.add(new Instructor(
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

        instructors.add(new Instructor(
                null,
                "רונית דביר",
                "0541234567",
                "ראשון לציון",
                "",
                4.3f,
                170,
                "אוטומט",
                "קיה פיקנטו",
                true
        ));

        return instructors;
    }
}