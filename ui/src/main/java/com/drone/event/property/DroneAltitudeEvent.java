package com.drone.event.property;

import com.drone.DroneProperty;
import com.drone.ui.GaugeConfiguration;

@GaugeConfiguration(property = DroneProperty.ALTITUDE, maxValue = 2000, precision = 0)
public class DroneAltitudeEvent extends AbstractDronePropertyEvent {

    public DroneAltitudeEvent(Object source, float altitude) {
        super(source, altitude);
    }

    @Override
    public DroneProperty getProperty() {
        return DroneProperty.ALTITUDE;
    }
}
