package com.example.easydriveproj;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Lesson {
    private String id;
    private String instructorId;
    private String studentId;
    private String studentName;
    private String studentPhone;
    private long startTime;
    private long endTime;
    private String location;
    private String status; // scheduled, completed, cancelled
    private String notes;
    private boolean paid;

    // Required empty constructor for Firebase
    public Lesson() {
    }

    public Lesson(String instructorId, String studentId, String studentName, String studentPhone,
                  long startTime, long endTime, String location, String status, String notes, boolean paid) {
        this.instructorId = instructorId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentPhone = studentPhone;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.status = status;
        this.notes = notes;
        this.paid = paid;
    }

    // Helper method to format the date for display
    public String getFormattedDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return dateFormat.format(new Date(startTime));
    }

    // Helper method to format the time for display
    public String getFormattedStartTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(new Date(startTime));
    }

    public String getFormattedEndTime() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(new Date(endTime));
    }

    // Helper method to get the duration in minutes
    public int getDurationMinutes() {
        return (int) ((endTime - startTime) / (1000 * 60));
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getInstructorId() { return instructorId; }
    public void setInstructorId(String instructorId) { this.instructorId = instructorId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentPhone() { return studentPhone; }
    public void setStudentPhone(String studentPhone) { this.studentPhone = studentPhone; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
}