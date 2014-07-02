package com.drone.event.property;

import com.drone.DroneProperty;
import com.drone.DroneTemplate;

public class DroneBatteryEvent extends AbstractDronePropertyEvent {
    private static final long serialVersionUID = 8095679751243142835L;

    public DroneBatteryEvent(DroneTemplate template, float batteryLevel) {
        super(template, batteryLevel);
    }

    @Override
    public DroneProperty getProperty() {
        return DroneProperty.BATTERY;
    }
}
