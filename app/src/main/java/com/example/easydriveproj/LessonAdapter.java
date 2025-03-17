package com.example.easydriveproj;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {
    private Context context;
    private List<Lesson> lessonList;

    public LessonAdapter(Context context, List<Lesson> lessonList) {
        this.context = context;
        this.lessonList = lessonList;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessonList.get(position);

        // Set data to views
        holder.studentNameTextView.setText(lesson.getStudentName());
        holder.dateTextView.setText(lesson.getFormattedDate());
        holder.timeTextView.setText(lesson.getFormattedStartTime() + " - " + lesson.getFormattedEndTime());
        holder.locationTextView.setText(lesson.getLocation());

        // Set status indicator
        switch (lesson.getStatus()) {
            case "scheduled":
                holder.statusIndicator.setImageResource(android.R.drawable.presence_online);
                break;
            case "completed":
                holder.statusIndicator.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
                break;
            case "cancelled":
                holder.statusIndicator.setImageResource(android.R.drawable.presence_busy);
                break;
            default:
                holder.statusIndicator.setImageResource(android.R.drawable.presence_invisible);
                break;
        }

        // Set payment status
        if (lesson.isPaid()) {
            holder.paymentStatusTextView.setText("שולם");
            holder.paymentStatusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
        } else {
            holder.paymentStatusTextView.setText("לא שולם");
            holder.paymentStatusTextView.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
        }

        // Set click listener to open detail activity
        holder.lessonCard.setOnClickListener(v -> {
            Intent intent = new Intent(context, LessonDetailActivity.class);
            intent.putExtra("LESSON_ID", lesson.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lessonList.size();
    }

    public static class LessonViewHolder extends RecyclerView.ViewHolder {
        CardView lessonCard;
        TextView studentNameTextView;
        TextView dateTextView;
        TextView timeTextView;
        TextView locationTextView;
        TextView paymentStatusTextView;
        ImageView statusIndicator;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);

            lessonCard = itemView.findViewById(R.id.lessonCard);
            studentNameTextView = itemView.findViewById(R.id.studentNameTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            paymentStatusTextView = itemView.findViewById(R.id.paymentStatusTextView);
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
        }
    }
}