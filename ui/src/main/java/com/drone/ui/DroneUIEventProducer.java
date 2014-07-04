package com.drone.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBusScope;
import org.vaadin.spring.events.EventScope;

import com.drone.DroneState;
import com.drone.DroneStateChangeCallback;
import com.drone.event.DroneEmergencyEvent;
import com.drone.event.DroneLowBatteryEvent;
import com.drone.event.property.DroneAltitudeEvent;
import com.drone.event.property.DroneBatteryEvent;
import com.drone.event.property.DroneThetaEvent;

@Component
class DroneUIEventProducer implements DroneStateChangeCallback {

    @Autowired
    @EventBusScope(EventScope.APPLICATION)
    private EventBus eventBus;

    @Override
    public void onDroneStateChanged(DroneState latestState) {
        if (latestState.isEmergency()) {
            eventBus.publish(this, new DroneEmergencyEvent());
        }
        if (latestState.isBatteryTooLow()) {
            eventBus.publish(this, new DroneLowBatteryEvent());
        }

        eventBus.publish(this,
                new DroneBatteryEvent(this, latestState.getBattery()));

        eventBus.publish(this,
                new DroneThetaEvent(this, latestState.getTheta()));

        eventBus.publish(this,
                new DroneAltitudeEvent(this, latestState.getAltitude()));
    }
}
