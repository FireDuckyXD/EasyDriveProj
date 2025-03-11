package com.example.easydriveproj;

import android.os.Bundle;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private Button priceFilter;
    private Button ratingFilter;
    private Button transmissionFilter;
    private Button carTypeFilter;
    private BottomNavigationView bottomNavigation;

    // Instructor-related variables
    private RecyclerView recyclerView;
    private InstructorAdapter adapter;
    private List<Instructor> instructorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        setupDropdownMenus();
        setupBottomNavigation();
        setupInstructorList();
    }

    private void initializeViews() {
        priceFilter = findViewById(R.id.priceFilter);
        ratingFilter = findViewById(R.id.ratingFilter);
        transmissionFilter = findViewById(R.id.transmissionFilter);
        carTypeFilter = findViewById(R.id.carTypeFilter);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Find RecyclerView
        recyclerView = findViewById(R.id.instructorsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupInstructorList() {
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

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.navigation_home);

        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    // Already in Home
                    return true;
                }
                else if (itemId == R.id.navigation_profile) {
                    // Navigate to Profile
                    Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    return true;
                }
                else if (itemId == R.id.nav_favorites) {
                    // Handle favorites navigation here
                    // Intent intent = new Intent(HomeActivity.this, FavoritesActivity.class);
                    // startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

    private void setupDropdownMenus() {
        // Price Filter
        priceFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPriceFilterMenu(view);
            }
        });

        // Rating Filter
        ratingFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRatingFilterMenu(view);
            }
        });

        // Transmission Filter
        transmissionFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTransmissionFilterMenu(view);
            }
        });

        // Car Type Filter
        carTypeFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCarTypeFilterMenu(view);
            }
        });
    }

    private void showPriceFilterMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.price_filter_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.price_low_to_high) {
                    // Handle low to high price filter
                    priceFilter.setText("מחיר: נמוך לגבוה");
                    return true;
                } else if (id == R.id.price_high_to_low) {
                    // Handle high to low price filter
                    priceFilter.setText("מחיר: גבוה לנמוך");
                    return true;
                }
                return false;
            }
        });

        popup.show();
    }

    private void showRatingFilterMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.rating_filter_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.rating_high_to_low) {
                    // Handle high to low rating filter
                    ratingFilter.setText("דירוג: גבוה לנמוך");
                    return true;
                } else if (id == R.id.rating_low_to_high) {
                    // Handle low to high rating filter
                    ratingFilter.setText("דירוג: נמוך לגבוה");
                    return true;
                }
                return false;
            }
        });

        popup.show();
    }

    private void showTransmissionFilterMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.transmission_filter_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.automatic) {
                    // Handle automatic transmission filter
                    transmissionFilter.setText("תיבת הילוכים: אוטומט");
                    return true;
                } else if (id == R.id.manual) {
                    // Handle manual transmission filter
                    transmissionFilter.setText("תיבת הילוכים: ידני");
                    return true;
                }
                return false;
            }
        });

        popup.show();
    }

    private void showCarTypeFilterMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.car_type_filter_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.private_car) {
                    // Handle private car filter
                    carTypeFilter.setText("סוג רכב: פרטי");
                    return true;
                } else if (id == R.id.truck) {
                    // Handle truck filter
                    carTypeFilter.setText("סוג רכב: משאית");
                    return true;
                } else if (id == R.id.motorcycle) {
                    // Handle motorcycle filter
                    carTypeFilter.setText("סוג רכב: אופנוע");
                    return true;
                }
                return false;
            }
        });

        popup.show();
    }
}