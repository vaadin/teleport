package com.drone.event;

public class DroneResetEmergencyEvent extends AbstractDroneTemplateEvent {
    private static final long serialVersionUID = 2512068613282346010L;

    public DroneResetEmergencyEvent(Object source) {
        super(source);
    }
}
