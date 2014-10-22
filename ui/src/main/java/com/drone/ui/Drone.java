package com.drone.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBusScope;
import org.vaadin.spring.events.EventScope;

import com.drone.DroneTemplate;

public class Drone {
    private static final double DEFAULT_MAX_SPEED = 25.0;
    private static final double DEFAULT_MAX_ALTITUDE = 1.0;

    private boolean flying;
    private double maxSpeed;
    private double maxAltitude;

    @Autowired
    private DroneTemplate template;

    @Autowired
    @EventBusScope(EventScope.APPLICATION)
    private EventBus eventBus;

    public Drone() {
        maxSpeed = DEFAULT_MAX_SPEED;
        maxAltitude = DEFAULT_MAX_ALTITUDE;
    }

    public void resetEmergency() {
        template.resetEmergency();
    }

    public boolean isFlying() {
        return flying;
    }

    @BroadcastDroneCommand
    public void setFlying(boolean flying) {
        this.flying = flying;

        if (flying) {
            template.takeoff();
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

    @BroadcastDroneCommand
    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public double getMaxAltitude() {
        return maxAltitude;
    }

    @BroadcastDroneCommand
    public void setMaxAltitude(double maxAltitude) {
        this.maxAltitude = maxAltitude;
    }
}
