package com.drone.ui;

public enum EmergencyType {
    ANGLES_EXCEEDED("Angles have exceeded"),
    USER_EMERGENCY("User declared emergency"),
    LOW_BATTERY("Low battery"),
    UNKNOWN("Unknown reason");

    private final String description;

    private EmergencyType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
