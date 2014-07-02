package com.drone.event;

import com.drone.DroneTemplate;

public class DroneLowBatteryEvent extends AbstractDroneEvent {
    private static final long serialVersionUID = 8880440612949757608L;

    public DroneLowBatteryEvent(DroneTemplate template) {
        super(template);
    }
}
