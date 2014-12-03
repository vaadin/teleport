package com.drone.event;

import com.drone.ui.EmergencyType;

public class DroneEmergencyEvent extends AbstractDroneEvent {

    private EmergencyType emergencyType;

    public DroneEmergencyEvent(EmergencyType emergencyType) {
        this.emergencyType = emergencyType;
    }

    public EmergencyType getEmergencyType() {
        return emergencyType;
    }
}
