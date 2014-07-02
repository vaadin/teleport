package com.drone.event;

import com.drone.DroneTemplate;

public class DroneControlUpdateEvent extends AbstractDroneEvent {
    private static final long serialVersionUID = 8095679751243142835L;

    public DroneControlUpdateEvent(DroneTemplate template) {
        super(template);
    }
}
