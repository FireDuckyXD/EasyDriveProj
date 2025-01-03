package com.example.easydriveproj;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;


public class HomeActivity extends AppCompatActivity {

    private MaterialButton priceFilter;
    private MaterialButton ratingFilter;
    private MaterialButton transmissionFilter;
    private MaterialButton carTypeFilter;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeViews();
        setupDropdownMenus();
    }

    private void initializeViews() {
        priceFilter = findViewById(R.id.priceFilter);
        ratingFilter = findViewById(R.id.ratingFilter);
        transmissionFilter = findViewById(R.id.transmissionFilter);
        carTypeFilter = findViewById(R.id.carTypeFilter);
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



