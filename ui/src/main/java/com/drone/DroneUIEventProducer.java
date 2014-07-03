package com.drone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBusScope;
import org.vaadin.spring.events.EventScope;

import com.drone.event.DroneEmergencyEvent;
import com.drone.event.DroneLowBatteryEvent;

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
    }
}
