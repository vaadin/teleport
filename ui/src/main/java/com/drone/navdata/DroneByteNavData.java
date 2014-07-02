package com.drone.navdata;

public class DroneByteNavData implements DroneNavData {
    private byte[] data;

    public DroneByteNavData(byte[] data) {
        this.data = data;
    }

    @Override
    public float getPitch() {
        return 0;
    }

    @Override
    public float getRoll() {
        return 0;
    }

    @Override
    public float getGaz() {
        return 0;
    }

    @Override
    public float getYaw() {
        return 0;
    }

    @Override
    public int getBattery() {
        return 0;
    }

    @Override
    public int getAltitude() {
        return 0;
    }
}
