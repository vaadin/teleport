package com.drone.event;

public abstract class AbstractDroneUIEvent extends AbstractDroneEvent {
	private static final long serialVersionUID = -5140262378969383781L;

	public AbstractDroneUIEvent(Object source) {
		super(source);
	}

	@Override
	public boolean publishToUI() {
		return true;
	}
}
