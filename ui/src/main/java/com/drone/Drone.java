package com.drone;

import org.springframework.beans.factory.annotation.Autowired;

public class Drone {
    private static final double DEFAULT_MAX_SPEED = 25.0;

    private boolean flying;
    private double maxSpeed;

    private DroneNavData navData;

    @Autowired
    private DroneTemplate template;

    public Drone() {
        maxSpeed = DEFAULT_MAX_SPEED;
        navData = new DroneNavData();
    }

    void setNavData(DroneNavData navData) {
        this.navData = navData;
    }

    public void resetEmergency() {
        template.resetEmergency();
    }

    public boolean isFlying() {
        return flying;
    }

    public void setFlying(boolean flying) {
        this.flying = flying;

        if (flying) {
            template.takeOff();
        } else {
            template.land();
        }
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    public float getMaxSpeedMultiplier() {
        return (float) (maxSpeed / 100f);
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
        template.setVelocity(maxSpeed);
    }

    public int getBattery() {
        return navData.getBattery();
    }

    public float getPitch() {
        return navData.getPitch();
    }

    public float getRoll() {
        return navData.getRoll();
    }

    public float getGaz() {
        return navData.getGaz();
    }

    public float getYaw() {
        return navData.getYaw();
    }

    public int getAltitude() {
        return navData.getAltitude();
    }

    public DroneNavData getNavData() {
        return navData;
    }
}
