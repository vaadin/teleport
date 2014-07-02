package com.drone.event.ui;

import com.drone.DroneTemplate;

public class DroneControlUpdateEvent extends AbstractDroneUIEvent {
    private static final long serialVersionUID = 8095679751243142835L;

    public DroneControlUpdateEvent(DroneTemplate template) {
        super(template);
    }
}
