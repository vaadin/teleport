package com.drone.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.annotation.VaadinComponent;
import org.vaadin.spring.annotation.VaadinUIScope;

import com.drone.DroneTemplate;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@VaadinComponent
@VaadinUIScope
public class DroneEmergencyDialog extends Window {
    private static final long serialVersionUID = -8190654181638447706L;

    @Autowired
    private DroneTemplate template;

    private Label text;

    public DroneEmergencyDialog() {
        super("EMERGENCY!");

        setResizable(false);
        setClosable(false);

        setWidth(300, Unit.PIXELS);
        setHeight(250, Unit.PIXELS);

        center();

        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();

        text = new Label("");

        Button reset = new Button("Reset", e -> {
            close();
            template.setResetEmergency();
        });

        layout.setMargin(true);
        layout.setSpacing(true);

        layout.addComponents(text, reset);
        reset.setWidth(100, Unit.PERCENTAGE);
        layout.setExpandRatio(reset, 1);
        layout.setComponentAlignment(reset, Alignment.BOTTOM_LEFT);

        setContent(layout);
    }

    public void show(EmergencyType type, UI ui) {
        this.text.setValue(type.getDescription());
        close();
        ui.addWindow(this);
    }
}