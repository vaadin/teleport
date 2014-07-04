package com.drone.event.property;

import com.drone.DroneProperty;
import com.drone.ui.GaugeConfiguration;

@GaugeConfiguration(property = DroneProperty.THETA, maxValue = 360, precision = 1)
public class DroneThetaEvent extends AbstractDronePropertyEvent {

    public DroneThetaEvent(Object source, float theta) {
        super(source, theta);
    }

    @Override
    public DroneProperty getProperty() {
        return DroneProperty.THETA;
    }
}
