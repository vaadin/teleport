package com.droid.ui.charts;

import java.math.BigDecimal;

public class BatteryLevelChangeEvent {
	private final float batteryLevel;

	/**
	 * @param batteryLevel
	 *            Battery level between 0 - 1
	 */
	public BatteryLevelChangeEvent(float batteryLevel) {
		this.batteryLevel = batteryLevel;
	}

	public int getBatteryLevel() {
		BigDecimal value = BigDecimal.valueOf(batteryLevel);
		value = value.multiply(BigDecimal.valueOf(100));
		value = value.setScale(2, BigDecimal.ROUND_HALF_EVEN);
		return value.intValue();
	}
}
