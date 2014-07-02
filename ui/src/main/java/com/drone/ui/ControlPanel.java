package com.drone.ui;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.UIScope;
import org.vaadin.spring.VaadinComponent;

import com.drone.Drone;
import com.vaadin.addon.touchkit.ui.Switch;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.PropertyId;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Slider;

@VaadinComponent
@UIScope
public class ControlPanel extends CustomComponent implements InitializingBean {
	private static final long serialVersionUID = 8067787477217415968L;

	private BeanFieldGroup<Drone> fieldGroup;
	private VerticalComponentGroup layout;

	@Autowired
	private Drone drone;

	@PropertyId("flying")
	private Switch flying;

	@PropertyId("maxSpeed")
	private Slider maxSpeed;

	public ControlPanel() {
		fieldGroup = new BeanFieldGroup<>(Drone.class);
		fieldGroup.setBuffered(false);

		layout = new VerticalComponentGroup("Controls");
		layout.setWidth(100, Unit.PERCENTAGE);

		flying = new Switch("Fly");

		maxSpeed = new Slider("Max speed");
		maxSpeed.setMin(0);
		maxSpeed.setMax(100);

		layout.addComponent(flying);
		layout.addComponent(maxSpeed);

		fieldGroup.bindMemberFields(this);
		
		addStyleName("control-panel");

		setCompositionRoot(layout);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		fieldGroup.setItemDataSource(drone);
	}
}