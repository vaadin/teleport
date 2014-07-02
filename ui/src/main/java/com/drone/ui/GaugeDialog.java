package com.drone.ui;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.drone.Drone;
import com.drone.GaugeConfiguration;
import com.drone.navdata.DroneNavData;
import com.drone.ui.charts.DataGauge;
import com.vaadin.addon.touchkit.ui.HorizontalButtonGroup;
import com.vaadin.addon.touchkit.ui.Popover;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.VerticalLayout;

public class GaugeDialog extends Popover {
    private static final long serialVersionUID = -2633336549420617084L;

    private GaugePanel panel;
    private Drone drone;

    private ComboBox gaugeSelector;

    private DataGauge gauge;

    public GaugeDialog(GaugePanel panel, Drone drone) {
        this(panel, drone, null);
    }

    public GaugeDialog(GaugePanel panel, Drone drone, DataGauge gauge) {
        this.panel = panel;
        this.gauge = gauge;
        this.drone = drone;

        setWidth(250, Unit.PIXELS);
        setResizable(false);
        setClosable(true);
        center();

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

        if (gaugeSelector.getContainerDataSource().size() > 0) {
            gaugeSelector.setValue(gaugeSelector.getContainerDataSource()
                    .getItemIds().iterator().next());
        }

        HorizontalButtonGroup buttonLayout = new HorizontalButtonGroup();

        Button add = new Button("Add", FontAwesome.PLUS);
        Button close = new Button("Cancel", FontAwesome.MINUS);
        Button remove = new Button("Remove", FontAwesome.TRASH_O);

        add.setVisible(gauge == null);
        gaugeSelector.setVisible(gauge == null);
        remove.setVisible(gauge != null);

        add.addClickListener(e -> onAddClicked());
        close.addClickListener(e -> onCloseClicked());
        remove.addClickListener(e -> onRemoveClicked());

        buttonLayout.addComponents(add, remove, close);
        layout.addComponent(buttonLayout);

        layout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);

        setContent(layout);
    }

    private void onRemoveClicked() {
        panel.removeGauge(gauge);
        close();
    }

    private void onAddClicked() {
        panel.addGauge((GaugeConfiguration) gaugeSelector.getValue());
        close();
    }

    private void onCloseClicked() {
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
