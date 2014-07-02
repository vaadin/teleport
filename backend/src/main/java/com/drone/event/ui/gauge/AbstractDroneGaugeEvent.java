package com.drone.event.ui.gauge;

import com.drone.DroneProperty;
import com.drone.event.ui.AbstractDroneUIEvent;

public abstract class AbstractDroneGaugeEvent extends AbstractDroneUIEvent {
    private static final long serialVersionUID = -8463982659175723940L;
    private final float value;

    public AbstractDroneGaugeEvent(Object source, float value) {
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
