package com.droid.navdata;

public class DroneBinaryNavData implements DroneNavData {
	private byte[] data;

	public DroneBinaryNavData(byte[] data) {
		this.data = data;
	}
	
	@Override
	public boolean isHavingEnoughBattery() {
		return false;
	}

	@Override
	public float getPitch() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getRoll() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getGaz() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getYaw() {
		// TODO Auto-generated method stub
		return 0;
	}



	

}
