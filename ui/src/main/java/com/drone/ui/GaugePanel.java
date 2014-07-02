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
import com.drone.event.property.AbstractDronePropertyEvent;
import com.drone.ui.charts.DataGauge;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;

@UIScope
@VaadinComponent
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

    private GaugeDialog gaugeDialog;

    public GaugePanel() {
        layout = new HorizontalLayout();
        layout.setWidth(100, Unit.PERCENTAGE);
        layout.setSpacing(true);

        gauges = new HashMap<>();

        setCompositionRoot(layout);

        Button addGauge = new Button(FontAwesome.PLUS);
        addGauge.addClickListener(e -> showGaugeDialog());

        gaugeLayout = new HorizontalLayout();
        gaugeLayout.setSpacing(true);

        layout.addComponent(gaugeLayout);
        layout.addComponents(addGauge);

        layout.setExpandRatio(gaugeLayout, 1);
        layout.setComponentAlignment(gaugeLayout, Alignment.TOP_RIGHT);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        eventBus.subscribe(this);
    }

    @Override
    public void destroy() throws Exception {
        eventBus.unsubscribe(this);
    }

    public void addGauge(GaugeConfiguration gaugeConfig) {
        if (gauges.containsKey(gaugeConfig.property())) {
            return;
        }

        DataGauge gauge = new DataGauge(gaugeConfig.property(),
                gaugeConfig.maxValue(), gaugeConfig.precision(), gaugeConfig
                        .property().getName());
        gauge.addClickListener(e -> showGaugeDialog(gauge));
        gaugeLayout.addComponentAsFirst(gauge);

        gauges.put(gaugeConfig.property(), gauge);
    }

    public void removeGauge(DataGauge gauge) {
        gaugeLayout.removeComponent(gauge);
        gauges.remove(gauge.getProperty());
    }

    private void showGaugeDialog() {
        showGaugeDialog(null);
    }

    private void showGaugeDialog(DataGauge gauge) {
        if (gaugeDialog != null) {
            gaugeDialog.close();
        }

        gaugeDialog = new GaugeDialog(this, drone, gauge);
        UI.getCurrent().addWindow(gaugeDialog);
    }

    @EventBusListenerMethod
    protected void onGaugeEvent(AbstractDronePropertyEvent event) {
        getUI().access(new Runnable() {
            @Override
            public void run() {
                DataGauge dataGauge = gauges.get(event.getProperty());
                if (dataGauge != null) {
                    dataGauge.setValue(event.getValue());
                }
            }
        });
    }
}
