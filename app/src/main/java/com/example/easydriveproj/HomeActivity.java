package com.example.easydriveproj;

import android.os.Bundle;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private EditText searchEditText;
    private Button priceFilter;
    private Button ratingFilter;
    private Button transmissionFilter;
    private Button carTypeFilter;
    private BottomNavigationView bottomNavigation;

    // Instructor-related variables
    private RecyclerView recyclerView;
    private InstructorAdapter adapter;
    private List<Instructor> instructorList;
    private List<Instructor> filteredInstructorList;

    // Filter states
    private String currentPriceFilter = "";
    private String currentRatingFilter = "";
    private String currentTransmissionFilter = "";
    private String currentCarTypeFilter = "";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        setupSearchFunctionality();
        setupDropdownMenus();
        setupBottomNavigation();
        setupInstructorList();
    }

    private void setupLongPressFilters() {
        // Long press on any filter button to clear all filters
        View.OnLongClickListener clearAllListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                clearAllFilters();
                Toast.makeText(HomeActivity.this, "כל הפילטרים נוקו", Toast.LENGTH_SHORT).show();
                return true;
            }
        };

        priceFilter.setOnLongClickListener(clearAllListener);
        ratingFilter.setOnLongClickListener(clearAllListener);
        transmissionFilter.setOnLongClickListener(clearAllListener);
        carTypeFilter.setOnLongClickListener(clearAllListener);
    }

    private void initializeViews() {
        searchEditText = findViewById(R.id.searchEditText);
        priceFilter = findViewById(R.id.priceFilter);
        ratingFilter = findViewById(R.id.ratingFilter);
        transmissionFilter = findViewById(R.id.transmissionFilter);
        carTypeFilter = findViewById(R.id.carTypeFilter);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Find RecyclerView
        recyclerView = findViewById(R.id.instructorsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Add clear all filters functionality when long pressing any filter button
        setupLongPressFilters();
    }

    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().toLowerCase().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Do nothing
            }
        });
    }

    private void setupInstructorList() {
        // Initialize the instructor lists
        instructorList = new ArrayList<>();
        filteredInstructorList = new ArrayList<>();

        // Initialize the adapter with empty list first
        adapter = new InstructorAdapter(this, filteredInstructorList);
        recyclerView.setAdapter(adapter);

        // Load data from Firebase
        loadInstructorsFromFirebase();
    }

    private void loadInstructorsFromFirebase() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Instructors");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                instructorList.clear();

                if (dataSnapshot.getChildrenCount() > 0) {
                    // Add the real instructors
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Instructor instructor = snapshot.getValue(Instructor.class);
                        if (instructor != null && instructor.isAvailable()) {
                            instructor.setId(snapshot.getKey());
                            instructorList.add(instructor);
                        }
                    }

                    // Apply current filters
                    applyFilters();

                    Toast.makeText(HomeActivity.this,
                            "נטענו " + instructorList.size() + " מורים מ-Firebase",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // No data in Firebase, add some sample data
                    addSampleDataToFirebase();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this,
                        "שגיאה בטעינת נתונים: " + databaseError.getMessage(),
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

        sampleInstructors.add(new Instructor(
                null,
                "רחל דוד",
                "0541112233",
                "ראשון לציון",
                "",
                4.3f,
                170,
                "אוטומט",
                "קיא פיקנטו",
                true
        ));

        sampleInstructors.add(new Instructor(
                null,
                "דני שמעון",
                "0523334455",
                "נתניה",
                "",
                4.8f,
                190,
                "ידני",
                "סקודה פאביה",
                true
        ));

        // Add them to Firebase
        for (int i = 0; i < sampleInstructors.size(); i++) {
            String key = "instructor-" + i;
            databaseRef.child(key).setValue(sampleInstructors.get(i));
        }

        Toast.makeText(this, "נוספו נתוני דוגמה ל-Firebase", Toast.LENGTH_SHORT).show();
    }

    private void applyFilters() {
        filteredInstructorList.clear();

        for (Instructor instructor : instructorList) {
            if (matchesFilters(instructor)) {
                filteredInstructorList.add(instructor);
            }
        }

        // Apply sorting based on current filters
        applySorting();

        // Update the adapter
        adapter.notifyDataSetChanged();

        // Show results count
        Toast.makeText(this, "נמצאו " + filteredInstructorList.size() + " מורים", Toast.LENGTH_SHORT).show();
    }

    private boolean matchesFilters(Instructor instructor) {
        // Search filter
        if (!currentSearchQuery.isEmpty()) {
            String searchQuery = currentSearchQuery.toLowerCase();
            if (!instructor.getName().toLowerCase().contains(searchQuery) &&
                    !instructor.getCity().toLowerCase().contains(searchQuery) &&
                    !instructor.getCarType().toLowerCase().contains(searchQuery)) {
                return false;
            }
        }

        // Transmission filter
        if (!currentTransmissionFilter.isEmpty()) {
            if (!instructor.getTransmissionType().equals(currentTransmissionFilter)) {
                return false;
            }
        }

        // Car type filter (improved filtering)
        if (!currentCarTypeFilter.isEmpty()) {
            String vehicleType = getVehicleType(instructor.getCarType());
            if (!vehicleType.equals(currentCarTypeFilter)) {
                return false;
            }
        }

        return true;
    }

    // Helper method to determine vehicle type from car type string
    private String getVehicleType(String carType) {
        if (carType == null) return "פרטי";

        String carTypeLower = carType.toLowerCase();
        if (carTypeLower.contains("משאית")) {
            return "משאית";
        } else if (carTypeLower.contains("אופנוע")) {
            return "אופנוע";
        } else {
            return "פרטי"; // Default to private car
        }
    }

    private void applySorting() {
        // Apply multiple sorts in priority order: rating first, then price
        // This allows both filters to work together

        if (!currentRatingFilter.isEmpty() && !currentPriceFilter.isEmpty()) {
            // Both rating and price filters active - sort by rating first, then by price for equal ratings
            Collections.sort(filteredInstructorList, new Comparator<Instructor>() {
                @Override
                public int compare(Instructor i1, Instructor i2) {
                    // Primary sort by rating
                    int ratingCompare;
                    if (currentRatingFilter.equals("גבוה לנמוך")) {
                        ratingCompare = Float.compare(i2.getRating(), i1.getRating());
                    } else {
                        ratingCompare = Float.compare(i1.getRating(), i2.getRating());
                    }

                    // If ratings are equal, sort by price
                    if (ratingCompare == 0) {
                        if (currentPriceFilter.equals("נמוך לגבוה")) {
                            return Integer.compare(i1.getPricePerLesson(), i2.getPricePerLesson());
                        } else {
                            return Integer.compare(i2.getPricePerLesson(), i1.getPricePerLesson());
                        }
                    }

                    return ratingCompare;
                }
            });
        } else if (!currentPriceFilter.isEmpty()) {
            // Only price filter active
            if (currentPriceFilter.equals("נמוך לגבוה")) {
                Collections.sort(filteredInstructorList, new Comparator<Instructor>() {
                    @Override
                    public int compare(Instructor i1, Instructor i2) {
                        return Integer.compare(i1.getPricePerLesson(), i2.getPricePerLesson());
                    }
                });
            } else if (currentPriceFilter.equals("גבוה לנמוך")) {
                Collections.sort(filteredInstructorList, new Comparator<Instructor>() {
                    @Override
                    public int compare(Instructor i1, Instructor i2) {
                        return Integer.compare(i2.getPricePerLesson(), i1.getPricePerLesson());
                    }
                });
            }
        } else if (!currentRatingFilter.isEmpty()) {
            // Only rating filter active
            if (currentRatingFilter.equals("גבוה לנמוך")) {
                Collections.sort(filteredInstructorList, new Comparator<Instructor>() {
                    @Override
                    public int compare(Instructor i1, Instructor i2) {
                        return Float.compare(i2.getRating(), i1.getRating());
                    }
                });
            } else if (currentRatingFilter.equals("נמוך לגבוה")) {
                Collections.sort(filteredInstructorList, new Comparator<Instructor>() {
                    @Override
                    public int compare(Instructor i1, Instructor i2) {
                        return Float.compare(i1.getRating(), i2.getRating());
                    }
                });
            }
        }
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
                    Toast.makeText(HomeActivity.this, "מועדפים - בקרוב", Toast.LENGTH_SHORT).show();
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
                    if (currentPriceFilter.equals("נמוך לגבוה")) {
                        // Same option clicked - cancel it
                        currentPriceFilter = "";
                        priceFilter.setText("מחיר");
                    } else {
                        currentPriceFilter = "נמוך לגבוה";
                        priceFilter.setText("מחיר: נמוך לגבוה");
                    }
                    applyFilters();
                    return true;
                } else if (id == R.id.price_high_to_low) {
                    if (currentPriceFilter.equals("גבוה לנמוך")) {
                        // Same option clicked - cancel it
                        currentPriceFilter = "";
                        priceFilter.setText("מחיר");
                    } else {
                        currentPriceFilter = "גבוה לנמוך";
                        priceFilter.setText("מחיר: גבוה לנמוך");
                    }
                    applyFilters();
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
                    if (currentRatingFilter.equals("גבוה לנמוך")) {
                        // Same option clicked - cancel it
                        currentRatingFilter = "";
                        ratingFilter.setText("דירוג");
                    } else {
                        currentRatingFilter = "גבוה לנמוך";
                        ratingFilter.setText("דירוג: גבוה לנמוך");
                    }
                    applyFilters();
                    return true;
                } else if (id == R.id.rating_low_to_high) {
                    if (currentRatingFilter.equals("נמוך לגבוה")) {
                        // Same option clicked - cancel it
                        currentRatingFilter = "";
                        ratingFilter.setText("דירוג");
                    } else {
                        currentRatingFilter = "נמוך לגבוה";
                        ratingFilter.setText("דירוג: נמוך לגבוה");
                    }
                    applyFilters();
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
                    if (currentTransmissionFilter.equals("אוטומט")) {
                        // Same option clicked - cancel it
                        currentTransmissionFilter = "";
                        transmissionFilter.setText("תיבת הילוכים");
                    } else {
                        currentTransmissionFilter = "אוטומט";
                        transmissionFilter.setText("תיבת הילוכים: אוטומט");
                    }
                    applyFilters();
                    return true;
                } else if (id == R.id.manual) {
                    if (currentTransmissionFilter.equals("ידני")) {
                        // Same option clicked - cancel it
                        currentTransmissionFilter = "";
                        transmissionFilter.setText("תיבת הילוכים");
                    } else {
                        currentTransmissionFilter = "ידני";
                        transmissionFilter.setText("תיבת הילוכים: ידני");
                    }
                    applyFilters();
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
                    if (currentCarTypeFilter.equals("פרטי")) {
                        // Same option clicked - cancel it
                        currentCarTypeFilter = "";
                        carTypeFilter.setText("סוג רכב");
                    } else {
                        currentCarTypeFilter = "פרטי";
                        carTypeFilter.setText("רכב פרטי");
                    }
                    applyFilters();
                    return true;
                } else if (id == R.id.truck) {
                    if (currentCarTypeFilter.equals("משאית")) {
                        // Same option clicked - cancel it
                        currentCarTypeFilter = "";
                        carTypeFilter.setText("סוג רכב");
                    } else {
                        currentCarTypeFilter = "משאית";
                        carTypeFilter.setText("משאית");
                    }
                    applyFilters();
                    return true;
                } else if (id == R.id.motorcycle) {
                    if (currentCarTypeFilter.equals("אופנוע")) {
                        // Same option clicked - cancel it
                        currentCarTypeFilter = "";
                        carTypeFilter.setText("סוג רכב");
                    } else {
                        currentCarTypeFilter = "אופנוע";
                        carTypeFilter.setText("אופנוע");
                    }
                    applyFilters();
                    return true;
                }
                return false;
            }
        });

        popup.show();
    }

    private void clearOtherSortFilters(String activeFilter) {
        // This method is no longer needed since we allow multiple filters
        // Keeping it for backward compatibility but making it do nothing
    }

    // Method to clear all filters
    private void clearAllFilters() {
        currentSearchQuery = "";
        currentPriceFilter = "";
        currentRatingFilter = "";
        currentTransmissionFilter = "";
        currentCarTypeFilter = "";

        searchEditText.setText("");
        priceFilter.setText("מחיר");
        ratingFilter.setText("דירוג");
        transmissionFilter.setText("תיבת הילוכים");
        carTypeFilter.setText("סוג רכב");

        applyFilters();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        if (adapter != null) {
            applyFilters();
        }
    }
}