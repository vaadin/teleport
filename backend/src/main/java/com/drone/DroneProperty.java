package com.drone;

public enum DroneProperty {
    ALTITUDE("Altitude"),
    BATTERY("Battery");

    private final String name;

    private DroneProperty(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
