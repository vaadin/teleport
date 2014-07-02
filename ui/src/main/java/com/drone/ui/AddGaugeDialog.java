package com.drone.ui;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.drone.Drone;
import com.drone.GaugeConfiguration;
import com.drone.navdata.DroneNavData;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class AddGaugeDialog extends Window {
    private static final long serialVersionUID = -2633336549420617084L;

    private GaugePanel panel;
    private Drone drone;

    private ComboBox gaugeSelector;

    public AddGaugeDialog(GaugePanel panel, Drone drone) {
        super("Add gauge");
        this.panel = panel;
        setWidth(200, Unit.PIXELS);
        setResizable(false);
        setClosable(true);
        center();

        this.drone = drone;

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);

        List<GaugeConfiguration> availableGauges = determineAvailableGauges();

        gaugeSelector = new ComboBox("Gauges");
        gaugeSelector.setNullSelectionAllowed(false);
        layout.addComponent(gaugeSelector);

        for (GaugeConfiguration configuration : availableGauges) {
            gaugeSelector.addItem(configuration);
            gaugeSelector.setItemCaption(configuration, configuration
                    .property().getName());
        }

        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        Button add = new Button(FontAwesome.PLUS);
        Button close = new Button(FontAwesome.MINUS);

        add.addClickListener(e -> onAddClicked());
        close.addClickListener(e -> close());

        buttonLayout.addComponents(add, close);
        layout.addComponent(buttonLayout);

        setContent(layout);
    }

    private void onAddClicked() {
        panel.addGauge((GaugeConfiguration) gaugeSelector.getValue());
        close();
    }

    private List<GaugeConfiguration> determineAvailableGauges() {
        List<GaugeConfiguration> droneProperties = new ArrayList<>();

        Class<?> navDataInterface = null;

        for (Class<?> droneInterface : drone.getNavData().getClass()
                .getInterfaces()) {
            if (droneInterface.equals(DroneNavData.class)) {
                navDataInterface = droneInterface;
            }
        }

        if (navDataInterface == null) {
            throw new RuntimeException(
                    "Drone's nav data does not implement DroneNavData interface");
        }

        for (Method method : navDataInterface.getMethods()) {
            if (method.isAnnotationPresent(GaugeConfiguration.class)) {
                droneProperties.add(method
                        .getAnnotation(GaugeConfiguration.class));
            }
        }

        return droneProperties;
    }
}
