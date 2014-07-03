package com.drone;

public interface DroneStateChangeCallback {
    void onDroneStateChanged(DroneState latestState);
}
