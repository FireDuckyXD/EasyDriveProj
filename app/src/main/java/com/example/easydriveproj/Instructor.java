package com.example.easydriveproj;

public class Instructor {
    private String id;
    private String name;
    private String phoneNumber;
    private String city;
    private String imageUrl;
    private float rating;
    private int pricePerLesson;
    private String transmissionType;
    private String carType;
    private boolean isAvailable;

    // Required empty constructor for Firebase
    public Instructor() {
        // Required empty constructor for Firebase
    }

    public Instructor(String id, String name, String phoneNumber, String city, String imageUrl,
                      float rating, int pricePerLesson, String transmissionType,
                      String carType, boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.city = city;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.pricePerLesson = pricePerLesson;
        this.transmissionType = transmissionType;
        this.carType = carType;
        this.isAvailable = isAvailable;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public int getPricePerLesson() { return pricePerLesson; }
    public void setPricePerLesson(int pricePerLesson) { this.pricePerLesson = pricePerLesson; }

    public String getTransmissionType() { return transmissionType; }
    public void setTransmissionType(String transmissionType) { this.transmissionType = transmissionType; }

    public String getCarType() { return carType; }
    public void setCarType(String carType) { this.carType = carType; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}