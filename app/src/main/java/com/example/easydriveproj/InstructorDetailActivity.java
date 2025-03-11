package com.example.easydriveproj;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class InstructorDetailActivity extends AppCompatActivity {

    private ImageView instructorImageView;
    private TextView nameTextView, cityTextView, priceTextView, transmissionTextView, carTypeTextView;
    private RatingBar ratingBar;
    private Button callButton, messageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_detail);

        // Set up toolbar with back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("פרטי מורה");

            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Initialize views
        instructorImageView = findViewById(R.id.instructorImageView);
        nameTextView = findViewById(R.id.nameTextView);
        cityTextView = findViewById(R.id.cityTextView);
        priceTextView = findViewById(R.id.priceTextView);
        ratingBar = findViewById(R.id.ratingBar);
        transmissionTextView = findViewById(R.id.transmissionTextView);
        carTypeTextView = findViewById(R.id.carTypeTextView);
        callButton = findViewById(R.id.callButton);
        messageButton = findViewById(R.id.messageButton);

        // Get instructor data from intent
        if (getIntent().hasExtra("INSTRUCTOR_ID")) {
            String instructorId = getIntent().getStringExtra("INSTRUCTOR_ID");
            String name = getIntent().getStringExtra("INSTRUCTOR_NAME");
            String phoneNumber = getIntent().getStringExtra("INSTRUCTOR_PHONE");
            String city = getIntent().getStringExtra("INSTRUCTOR_CITY");
            float rating = getIntent().getFloatExtra("INSTRUCTOR_RATING", 0f);
            int price = getIntent().getIntExtra("INSTRUCTOR_PRICE", 0);
            String transmission = getIntent().getStringExtra("INSTRUCTOR_TRANSMISSION");
            String carType = getIntent().getStringExtra("INSTRUCTOR_CAR_TYPE");

            // Display data
            displayInstructorData(name, phoneNumber, city, rating, price, transmission, carType);
        } else {
            Toast.makeText(this, "שגיאה: מידע מורה חסר", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void displayInstructorData(String name, String phoneNumber, String city,
                                       float rating, int price, String transmission, String carType) {
        // Set instructor data to views
        nameTextView.setText(name);
        cityTextView.setText(city);
        priceTextView.setText(String.format("%d ₪ לשיעור", price));
        ratingBar.setRating(rating);
        transmissionTextView.setText(transmission);
        carTypeTextView.setText(carType);

        // Set default image for instructor
        instructorImageView.setImageResource(android.R.drawable.ic_menu_camera);

        // Set up call button
        callButton.setOnClickListener(v -> {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            } else {
                Toast.makeText(this, "מספר טלפון לא זמין", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up message button
        messageButton.setOnClickListener(v -> {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:" + phoneNumber));
                intent.putExtra("sms_body", "שלום, אני מעוניין/ת בשיעור נהיגה");
                startActivity(intent);
            } else {
                Toast.makeText(this, "מספר טלפון לא זמין", Toast.LENGTH_SHORT).show();
            }
        });
    }
}