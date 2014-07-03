package com.drone.event.property;

import com.drone.DroneProperty;
import com.drone.DroneTemplate;

public class DroneAltitudeEvent extends AbstractDronePropertyEvent {

    public DroneAltitudeEvent(DroneTemplate template, float batteryLevel) {
        super(template, batteryLevel);
    }

    @Override
    public DroneProperty getProperty() {
        return DroneProperty.ALTITUDE;
    }
}
