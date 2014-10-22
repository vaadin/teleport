package com.drone;

public enum DroneProperty {
    ALTITUDE("Altitude"),
    BATTERY("Battery"),
    WIFI_LINK_QUALITY("Wifi"),
    THETA("Heading theta");

    private final String name;

    private DroneProperty(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
