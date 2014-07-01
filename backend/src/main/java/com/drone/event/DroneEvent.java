package com.drone.event;

import org.springframework.context.ApplicationEvent;

public abstract class DroneEvent extends ApplicationEvent {
	private static final long serialVersionUID = 24526849032106624L;

	public DroneEvent(Object source) {
		super(source);
	}
}
