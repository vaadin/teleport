package com.drone;

public class DroneNavData {

    @GaugeConfiguration(property = DroneProperty.BATTERY, maxValue = 100, precision = 0)
    public int getBattery() {
        return 0;
    }

    @GaugeConfiguration(property = DroneProperty.ALTITUDE, maxValue = 6, precision = 3)
    public int getAltitude() {
        return 0;
    }

    public float getPitch() {
        return 0;
    }

    public float getRoll() {
        return 0;
    }

    public float getGaz() {
        return 0;
    }

    public float getYaw() {
        return 0;
    }
}
