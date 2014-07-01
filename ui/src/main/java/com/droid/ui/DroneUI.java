package com.droid.ui;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.touchkit.TouchKitUI;

import com.droid.DroneTemplate;
import com.droid.ui.charts.BatteryLevelChangeEvent;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.jogdial.JogDial;
import com.vaadin.jogdial.client.Position;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

@TouchKitUI
@Theme("droid")
@PreserveOnRefresh
public class DroneUI extends UI  {
	private static final long serialVersionUID = 6337889226477810842L;

	@Autowired
	private DroneTemplate service;

	@Autowired
	private ControlPanel controlPanel;

	private VerticalLayout mainLayout;

	@Autowired
	private EventBus eventBus;

	@Override
	protected void init(VaadinRequest request) {
		setSizeFull();

		mainLayout = new VerticalLayout();
		mainLayout.setMargin(true);
		mainLayout.setSizeFull();

		mainLayout.addComponent(new Button("Test", new Button.ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				eventBus.publish(this,
						new BatteryLevelChangeEvent((float) Math.random()));
			}
		}));

		JogDial rotation = new JogDial(Position.LEFT, 150);
		rotation.addAxesMoveListener(e -> service.rotateByAxis(e.getX() * -1));

		JogDial movement = new JogDial(Position.RIGHT, 150);
		movement.addAxesMoveListener(e -> service.moveByAxis(e.getX() * -1,
				e.getY() * -1));

		HorizontalLayout jogDialLayout = new HorizontalLayout();
		jogDialLayout.setWidth(100, Unit.PERCENTAGE);
		jogDialLayout.addComponents(rotation, movement);
		jogDialLayout.setExpandRatio(movement, 1);
		jogDialLayout.setComponentAlignment(movement, Alignment.BOTTOM_RIGHT);

		mainLayout.addComponents(controlPanel, jogDialLayout);
		mainLayout.setExpandRatio(jogDialLayout, 1);
		mainLayout.setComponentAlignment(jogDialLayout, Alignment.BOTTOM_LEFT);

		setContent(mainLayout);
	}
}
