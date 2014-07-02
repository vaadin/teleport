package com.drone.ui;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.drone.Drone;
import com.drone.GaugeConfiguration;
import com.drone.navdata.DroneNavData;
import com.vaadin.addon.touchkit.ui.HorizontalButtonGroup;
import com.vaadin.addon.touchkit.ui.Popover;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.VerticalLayout;

public class AddGaugeDialog extends Popover {
    private static final long serialVersionUID = -2633336549420617084L;

    private GaugePanel panel;
    private Drone drone;

    private ComboBox gaugeSelector;

    public AddGaugeDialog(GaugePanel panel, Drone drone) {
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
        gaugeSelector.setWidth(100, Unit.PERCENTAGE);
        gaugeSelector.setNullSelectionAllowed(false);
        layout.addComponent(gaugeSelector);

        for (GaugeConfiguration configuration : availableGauges) {
            gaugeSelector.addItem(configuration);
            gaugeSelector.setItemCaption(configuration, configuration
                    .property().getName());
        }

        HorizontalButtonGroup buttonLayout = new HorizontalButtonGroup();

        Button add = new Button(FontAwesome.PLUS);
        Button close = new Button(FontAwesome.MINUS);

        add.addClickListener(e -> onAddClicked());
        close.addClickListener(e -> close());

        buttonLayout.addComponents(add, close);
        layout.addComponent(buttonLayout);

        layout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);

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
