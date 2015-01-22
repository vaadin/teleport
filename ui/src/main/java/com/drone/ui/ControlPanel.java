package com.drone.ui;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.annotation.VaadinComponent;
import org.vaadin.spring.annotation.VaadinUIScope;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.annotation.EventBusListenerMethod;

import com.drone.event.DroneControlUpdateEvent;
import com.drone.event.DroneEmergencyEvent;
import com.vaadin.addon.touchkit.ui.Switch;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Slider;

@VaadinComponent
@VaadinUIScope
public class ControlPanel extends CustomComponent implements InitializingBean,
        DisposableBean {
    private static final long serialVersionUID = 8067787477217415968L;

    private BeanFieldGroup<Drone> fieldGroup;
    private VerticalComponentGroup layout;

    @Autowired
    private Drone drone;

    @PropertyId("flying")
    private Switch flying;

    @PropertyId("maxSpeed")
    private Slider maxSpeed;

    @PropertyId("maxAltitude")
    private Slider maxAltitude;

    @Autowired
    private EventBus eventBus;

    public ControlPanel() {
        fieldGroup = new BeanFieldGroup<>(Drone.class);
        fieldGroup.setBuffered(false);

        layout = new VerticalComponentGroup("Controls");
        layout.setWidth(100, Unit.PERCENTAGE);

        flying = new Switch("Fly");

        Button emergency = new Button("Emergency");
        emergency.addClickListener(e -> drone.declareEmergency());

        maxSpeed = new Slider("Max speed");
        maxSpeed.setMin(0);
        maxSpeed.setMax(100);

        maxAltitude = new Slider("Max altitude");
        maxAltitude.setMin(0);
        maxAltitude.setMax(4);

        layout.addComponents(flying, emergency, maxSpeed, maxAltitude);

        fieldGroup.bindMemberFields(this);

        addStyleName("control-panel");

        setCompositionRoot(layout);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        fieldGroup.setItemDataSource(drone);
        eventBus.subscribe(this);
    }

    @Override
    public void destroy() throws Exception {
        eventBus.unsubscribe(this);
    }

    @EventBusListenerMethod
    protected void onDroneControlUpdate(DroneControlUpdateEvent event) {
        getUI().access(() -> fieldGroup.setItemDataSource(drone));
    }

    @EventBusListenerMethod
    protected void onEmergency(DroneEmergencyEvent emergencyEvent) {
        drone.setFlying(false);
        getUI().access(() -> fieldGroup.setItemDataSource(drone));
    }
}