package com.drone.event.property;

import com.drone.DroneProperty;
import com.drone.event.AbstractDroneEvent;

public abstract class AbstractDronePropertyEvent extends AbstractDroneEvent {
    private static final long serialVersionUID = -8463982659175723940L;
    private final float value;

    public AbstractDronePropertyEvent(Object source, float value) {
        super(source);

        this.value = value;
    }

    public abstract DroneProperty getProperty();

    /**
     * @return Gauge value between [0 - 1]
     */
    public float getValue() {
        return value;
    }
}
