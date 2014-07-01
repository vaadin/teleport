package com.drone;

import org.springframework.beans.factory.annotation.Autowired;

import com.drone.navdata.DroneByteNavData;
import com.drone.navdata.DroneNavData;

public class Drone implements DroneNavData {
	private static final double DEFAULT_MAX_SPEED = 25.0;

	private boolean flying;
	private double maxSpeed;

	private DroneNavData navData;
	
	@Autowired
	private DroneTemplate template;

	public Drone() {
		maxSpeed = DEFAULT_MAX_SPEED;
		navData = new DroneByteNavData(null);
	}

	void setNavData(DroneNavData navData) {
		this.navData = navData;
	}
	
	public boolean isFlying() {
		return flying;
	}

	public void setFlying(boolean flying) {
		this.flying = flying;
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public float getMaxSpeedMultiplier() {
		return (float) (maxSpeed / 100f);
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
		template.setVelocity(maxSpeed);
	}

	public double getBattery() {
		return navData.getBattery();
	}

	@Override
	public float getPitch() {
		return navData.getPitch();
	}

	@Override
	public float getRoll() {
		return navData.getRoll();
	}

	@Override
	public float getGaz() {
		return navData.getGaz();
	}

	@Override
	public float getYaw() {
		return navData.getYaw();
	}
}
