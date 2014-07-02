package com.drone.event;

import com.drone.DroneTemplate;

public class DroneEmergencyEvent extends AbstractDroneEvent {
    private static final long serialVersionUID = 4699345507126074335L;

    public DroneEmergencyEvent(DroneTemplate template) {
        super(template);
    }
}
