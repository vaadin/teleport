package com.drone.navdata;

public interface DroneNavData {

    /**
     * @return Battery level of drone [0 - 100]
     */
    public int getBattery();

    public float getPitch();

    public float getRoll();

    public float getGaz();

    public float getYaw();
}
