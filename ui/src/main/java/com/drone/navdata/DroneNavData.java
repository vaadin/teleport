package com.drone.navdata;

import com.drone.DroneProperty;
import com.drone.GaugeConfiguration;

public interface DroneNavData {

    @GaugeConfiguration(property = DroneProperty.BATTERY, maxValue = 100, precision = 0)
    public int getBattery();

    @GaugeConfiguration(property = DroneProperty.ALTITUDE, maxValue = 6, precision = 3)
    public int getAltitude();

    public float getPitch();

    public float getRoll();

    public float getGaz();

    public float getYaw();
}
