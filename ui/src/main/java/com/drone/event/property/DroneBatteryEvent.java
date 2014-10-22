package com.drone.event.property;

import com.drone.DroneProperty;
import com.drone.ui.GaugeConfiguration;

@GaugeConfiguration(property = DroneProperty.BATTERY, maxValue = 100, precision = 0)
public class DroneBatteryEvent extends AbstractDronePropertyEvent {

    public DroneBatteryEvent(Object source, int batteryLevel) {
        super(source, batteryLevel);
    }

    @Override
    public DroneProperty getProperty() {
        return DroneProperty.BATTERY;
    }
}
