package com.drone.event.property;

import com.drone.DroneProperty;
import com.drone.ui.GaugeConfiguration;

@GaugeConfiguration(property = DroneProperty.WIFI_LINK_QUALITY, maxValue = 500, precision = 0)
public class DroneWIFILinkQualityEvent extends AbstractDronePropertyEvent {

    public DroneWIFILinkQualityEvent(Object source, long linkQuality) {
        super(source, linkQuality);
    }

    @Override
    public DroneProperty getProperty() {
        return DroneProperty.THETA;
    }
}
