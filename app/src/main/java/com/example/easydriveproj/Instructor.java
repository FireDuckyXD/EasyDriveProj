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
    private String vehicleType; // New field for vehicle category
    private boolean isAvailable;

    // Required empty constructor for Firebase
    public Instructor() {
        // Required empty constructor for Firebase
    }

    // Updated constructor to include vehicleType
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

        // Auto-determine vehicle type from car type
        this.vehicleType = determineVehicleType(carType);
    }

    // Constructor with explicit vehicle type
    public Instructor(String id, String name, String phoneNumber, String city, String imageUrl,
                      float rating, int pricePerLesson, String transmissionType,
                      String carType, String vehicleType, boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.city = city;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.pricePerLesson = pricePerLesson;
        this.transmissionType = transmissionType;
        this.carType = carType;
        this.vehicleType = vehicleType;
        this.isAvailable = isAvailable;
    }

    // Helper method to determine vehicle type from car type string
    private String determineVehicleType(String carType) {
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

    // Method to get vehicle type display name
    public String getVehicleTypeDisplay() {
        switch (vehicleType) {
            case "משאית":
                return "משאית";
            case "אופנוע":
                return "אופנוע";
            case "פרטי":
            default:
                return "רכב פרטי";
        }
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
    public void setCarType(String carType) {
        this.carType = carType;
        // Update vehicle type when car type changes
        this.vehicleType = determineVehicleType(carType);
    }

    public String getVehicleType() {
        // Ensure vehicle type is set based on car type if it's null
        if (vehicleType == null && carType != null) {
            vehicleType = determineVehicleType(carType);
        }
        return vehicleType != null ? vehicleType : "פרטי";
    }

    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
}