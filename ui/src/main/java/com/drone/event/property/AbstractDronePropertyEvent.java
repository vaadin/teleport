package com.drone.event.property;

import com.drone.DroneProperty;

public abstract class AbstractDronePropertyEvent {
    private final float value;

    public AbstractDronePropertyEvent(Object source, float value) {
        this.value = value;
    }

    /**
     * @return Gauge value between [0 - 1]
     */
    public float getValue() {
        return value;
    }

    public abstract DroneProperty getProperty();
}
