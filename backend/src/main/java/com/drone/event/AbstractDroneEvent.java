package com.drone.event;

import org.springframework.context.ApplicationEvent;

public abstract class AbstractDroneEvent extends ApplicationEvent {
    private static final long serialVersionUID = 24526849032106624L;

    public AbstractDroneEvent(Object source) {
        super(source);
    }
}
