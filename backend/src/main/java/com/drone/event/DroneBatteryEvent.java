package com.drone.event;

import com.drone.DroneTemplate;

public class DroneBatteryEvent extends AbstractDroneUIEvent {
	private static final long serialVersionUID = 8095679751243142835L;
	private final int batteryLevel;

	public DroneBatteryEvent(DroneTemplate template, int batteryLevel) {
		super(template);
		this.batteryLevel = batteryLevel;
	}

	public int getBatteryLevel() {
		return batteryLevel;
	}
}
