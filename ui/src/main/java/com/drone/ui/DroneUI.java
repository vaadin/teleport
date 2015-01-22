package com.drone.ui;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;
import org.vaadin.spring.touchkit.annotation.TouchKitUI;

import com.drone.DroneTemplate;
import com.drone.event.DroneEmergencyEvent;
import com.drone.event.DroneLowBatteryEvent;
import com.vaadin.annotations.Theme;
import com.vaadin.jogdial.JogDial;
import com.vaadin.jogdial.client.Position;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@TouchKitUI
@Theme("drone")
public class DroneUI extends UI implements InitializingBean, DisposableBean {
    private static final long serialVersionUID = 6337889226477810842L;

    @Autowired
    private DroneTemplate service;

    @Autowired
    private ControlPanel controlPanel;

    @Autowired
    private GaugePanel gaugePanel;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private DroneEmergencyDialog emergencyDialog;

    private VerticalLayout mainLayout;

    private float yaw, pitch, roll, gaz;

    @Override
    protected void init(VaadinRequest request) {
        UI.getCurrent().setPollInterval(1000);

        setSizeFull();

        mainLayout = new VerticalLayout();
        mainLayout.setMargin(true);
        mainLayout.setSizeFull();

        JogDial rotation = new JogDial(Position.LEFT, 150);
        rotation.addAxesMoveListener(e -> {
            yaw = -e.getX();
            gaz = e.getY();
            service.move(yaw, pitch, roll, gaz);
        });

        JogDial movement = new JogDial(Position.RIGHT, 150);
        movement.addAxesMoveListener(e -> {
            pitch = -e.getX();
            roll = -e.getY();
            service.move(yaw, pitch, roll, gaz);
        });

        HorizontalLayout jogDialLayout = new HorizontalLayout();
        jogDialLayout.setWidth(100, Unit.PERCENTAGE);
        jogDialLayout.addComponents(rotation, movement);
        jogDialLayout.setExpandRatio(movement, 1);
        jogDialLayout.setComponentAlignment(movement, Alignment.BOTTOM_RIGHT);

        controlPanel.setWidth(200, Unit.PIXELS);
        gaugePanel.setWidth(100, Unit.PERCENTAGE);

        HorizontalLayout topLayout = new HorizontalLayout();
        topLayout.setWidth(100, Unit.PERCENTAGE);
        topLayout.addComponents(controlPanel, gaugePanel);

        topLayout.setExpandRatio(gaugePanel, 1);

        mainLayout.addComponents(topLayout, jogDialLayout);
        mainLayout.setExpandRatio(jogDialLayout, 1);
        mainLayout.setComponentAlignment(jogDialLayout, Alignment.BOTTOM_LEFT);

        setContent(mainLayout);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        eventBus.subscribe(this);
    }

    @Override
    public void destroy() throws Exception {
        eventBus.unsubscribe(this);
    }

    @EventBusListenerMethod
    protected void onEmergencyEvent(DroneEmergencyEvent event) {
        getUI().access(
                () -> emergencyDialog.show(event.getEmergencyType(), getUI()));
    }

    @EventBusListenerMethod
    protected void onBatterLowEvent(DroneLowBatteryEvent event) {
        getUI().access(
                () -> emergencyDialog.show(EmergencyType.LOW_BATTERY, getUI()));
    }
}
