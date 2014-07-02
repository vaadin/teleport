package com.drone.event.ui;

import com.drone.DroneTemplate;

public class DroneEmergencyEvent extends AbstractDroneUIEvent {
    private static final long serialVersionUID = 8880440612949757608L;

    public DroneEmergencyEvent(DroneTemplate template) {
        super(template);
    }
}
