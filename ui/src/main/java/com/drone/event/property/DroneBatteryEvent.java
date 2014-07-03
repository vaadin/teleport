package com.drone.event.property;

import com.drone.DroneProperty;
import com.drone.DroneTemplate;

public class DroneBatteryEvent extends AbstractDronePropertyEvent {

    public DroneBatteryEvent(DroneTemplate template, float batteryLevel) {
        super(template, batteryLevel);
    }

    @Override
    public DroneProperty getProperty() {
        return DroneProperty.BATTERY;
    }
}
