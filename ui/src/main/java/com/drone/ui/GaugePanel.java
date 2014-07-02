package com.drone.ui;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.UIScope;
import org.vaadin.spring.VaadinComponent;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBusListenerMethod;

import com.drone.Drone;
import com.drone.DroneProperty;
import com.drone.GaugeConfiguration;
import com.drone.event.ui.gauge.AbstractDroneGaugeEvent;
import com.drone.ui.charts.DataGauge;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;

@VaadinComponent
@UIScope
public class GaugePanel extends CustomComponent implements InitializingBean,
        DisposableBean {
    private static final long serialVersionUID = 4035048387445957610L;

    private HorizontalLayout layout;
    private HorizontalLayout gaugeLayout;

    private Map<DroneProperty, DataGauge> gauges;

    @Autowired
    private Drone drone;

    @Autowired
    private EventBus eventBus;

    public GaugePanel() {
        layout = new HorizontalLayout();
        layout.setWidth(100, Unit.PERCENTAGE);
        layout.setSpacing(true);

        gauges = new HashMap<>();

        setCompositionRoot(layout);

        Button addGauge = new Button(FontAwesome.PLUS);
        addGauge.addClickListener(e -> showAddGaugeDialog());

        gaugeLayout = new HorizontalLayout();
        gaugeLayout.setSpacing(true);

        layout.addComponent(gaugeLayout);
        layout.addComponents(addGauge);

        layout.setExpandRatio(gaugeLayout, 1);
        layout.setComponentAlignment(gaugeLayout, Alignment.TOP_RIGHT);
    }

    private void showAddGaugeDialog() {
        AddGaugeDialog addGaugeDialog = new AddGaugeDialog(this, drone);
        UI.getCurrent().addWindow(addGaugeDialog);
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
    protected void onGaugeEvent(AbstractDroneGaugeEvent event) {
        DataGauge dataGauge = gauges.get(event.getProperty());

        if (dataGauge != null) {
            getUI().access(new Runnable() {
                @Override
                public void run() {
                    dataGauge.setValue(event.getValue());
                }
            });
        }
    }

    public void addGauge(GaugeConfiguration gaugeConfig) {
        if (gauges.containsKey(gaugeConfig.property())) {
            return;
        }

        DataGauge gauge = new DataGauge(gaugeConfig.property(),
                gaugeConfig.maxValue(), gaugeConfig.precision(), gaugeConfig
                        .property().getName());
        gaugeLayout.addComponentAsFirst(gauge);

        gauges.put(gaugeConfig.property(), gauge);
    }
}
