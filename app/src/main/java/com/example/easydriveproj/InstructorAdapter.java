package com.example.easydriveproj;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InstructorAdapter extends RecyclerView.Adapter<InstructorAdapter.InstructorViewHolder> {
    private static final String TAG = "InstructorAdapter";

    private Context context;
    private List<Instructor> instructorList;

    public InstructorAdapter(Context context, List<Instructor> instructorList) {
        this.context = context;
        this.instructorList = instructorList;
    }

    @NonNull
    @Override
    public InstructorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_instructor, parent, false);
        return new InstructorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstructorViewHolder holder, int position) {
        Instructor instructor = instructorList.get(position);

        // Set data to views
        holder.instructorName.setText(instructor.getName());
        holder.price.setText(String.format("%d ₪", instructor.getPricePerLesson()));
        holder.ratingBar.setRating(instructor.getRating());
        holder.carInfo.setText(instructor.getCarType() + " • " + instructor.getTransmissionType());

        // Set a default image for all instructors
        holder.instructorImage.setImageResource(android.R.drawable.ic_menu_camera);

        // Set click listener to open detail activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, InstructorDetailActivity.class);
            intent.putExtra("INSTRUCTOR_ID", instructor.getId());
            intent.putExtra("INSTRUCTOR_NAME", instructor.getName());
            intent.putExtra("INSTRUCTOR_PHONE", instructor.getPhoneNumber());
            intent.putExtra("INSTRUCTOR_CITY", instructor.getCity());
            intent.putExtra("INSTRUCTOR_RATING", instructor.getRating());
            intent.putExtra("INSTRUCTOR_PRICE", instructor.getPricePerLesson());
            intent.putExtra("INSTRUCTOR_TRANSMISSION", instructor.getTransmissionType());
            intent.putExtra("INSTRUCTOR_CAR_TYPE", instructor.getCarType());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return instructorList.size();
    }

    // ViewHolder class
    public static class InstructorViewHolder extends RecyclerView.ViewHolder {
        ImageView instructorImage;
        TextView instructorName, price, carInfo;
        RatingBar ratingBar;

        public InstructorViewHolder(@NonNull View itemView) {
            super(itemView);
            // These IDs should match your item_instructor.xml
            instructorImage = itemView.findViewById(R.id.instructorImage);
            instructorName = itemView.findViewById(R.id.instructorName);
            price = itemView.findViewById(R.id.price);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            carInfo = itemView.findViewById(R.id.carInfo);
        }
    }
}