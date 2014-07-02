package com.drone.ui;

import com.drone.DroneTemplate;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

public class DroneEmergencyDialog extends Window {
	private static final long serialVersionUID = -8190654181638447706L;

	private DroneTemplate template;

	public DroneEmergencyDialog(DroneTemplate service) {
		super("EMERGENCY!");

		this.template = service;

		setResizable(false);
		setClosable(false);

		setWidth(300, Unit.PIXELS);
		setHeight(250, Unit.PIXELS);

		center();

		VerticalLayout layout = new VerticalLayout();
		layout.setSizeFull();

		Label text = new Label(
				"Drone is in emergency state due to exceeded angular value, "
						+ "battery level or crash. Please press reset button to "
						+ "restart or wait for the flight operator to restart the drone.");

		Button reset = new Button("Reset", new Button.ClickListener() {
			private static final long serialVersionUID = 7823977356718657023L;

			@Override
			public void buttonClick(ClickEvent event) {
				close();
				template.resetEmergency();
			}
		});

		layout.setMargin(true);
		layout.setSpacing(true);

		layout.addComponents(text, reset);
		reset.setWidth(100, Unit.PERCENTAGE);
		layout.setExpandRatio(reset, 1);
		layout.setComponentAlignment(reset, Alignment.BOTTOM_LEFT);

		setContent(layout);
	}

	public void show() {
		close();
		UI.getCurrent().addWindow(this);
	}
}