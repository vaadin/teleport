package com.drone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBusScope;
import org.vaadin.spring.events.EventScope;

import com.drone.event.AbstractDroneEvent;
import com.drone.navdata.DroneByteNavData;
import com.drone.navdata.DroneNavData;

public class Drone implements DroneNavData,
        ApplicationListener<AbstractDroneEvent> {
    private static final double DEFAULT_MAX_SPEED = 25.0;

    private boolean flying;
    private double maxSpeed;

    private DroneNavData navData;

    @Autowired
    @EventBusScope(value = EventScope.APPLICATION)
    private EventBus eventBus;

    @Autowired
    private DroneTemplate template;

    public Drone() {
        maxSpeed = DEFAULT_MAX_SPEED;
        navData = new DroneByteNavData(null);
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

    @Override
    public int getBattery() {
        return navData.getBattery();
    }

    @Override
    public float getPitch() {
        return navData.getPitch();
    }

    @Override
    public float getRoll() {
        return navData.getRoll();
    }

    @Override
    public float getGaz() {
        return navData.getGaz();
    }

    @Override
    public float getYaw() {
        return navData.getYaw();
    }

    @Override
    public void onApplicationEvent(AbstractDroneEvent event) {
        if (event.publishToUI()) {
            eventBus.publish(EventScope.APPLICATION, event.getSource(), event);
        }
    }

    @Override
    public int getAltitude() {
        return navData.getAltitude();
    }

    public DroneNavData getNavData() {
        return navData;
    }
}
