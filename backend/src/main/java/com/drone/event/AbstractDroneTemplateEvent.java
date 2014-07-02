package com.drone.event;

public abstract class AbstractDroneTemplateEvent extends AbstractDroneEvent {
	private static final long serialVersionUID = -2041978398544578337L;

	public AbstractDroneTemplateEvent(Object source) {
		super(source);
	}

	@Override
	public boolean publishToUI() {
		return false;
	}
}
