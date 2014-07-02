package com.drone.event.ui.gauge;

import com.drone.DroneProperty;
import com.drone.DroneTemplate;

public class DroneAltitudeEvent extends AbstractDroneGaugeEvent {
    private static final long serialVersionUID = 8095679751243142835L;

    public DroneAltitudeEvent(DroneTemplate template, float batteryLevel) {
        super(template, batteryLevel);
    }

    @Override
    public DroneProperty getProperty() {
        return DroneProperty.ALTITUDE;
    }
}
