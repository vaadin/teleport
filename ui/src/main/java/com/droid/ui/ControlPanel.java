package com.droid.ui;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.UIScope;
import org.vaadin.spring.VaadinComponent;

import com.droid.DroneTemplate;
import com.droid.ui.charts.BatteryLevelGauge;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.touchkit.ui.Switch;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CustomComponent;

@VaadinComponent
@UIScope
public class ControlPanel extends CustomComponent implements InitializingBean {
	private static final long serialVersionUID = 8067787477217415968L;

	private VerticalComponentGroup layout;

	@Autowired
	private DroneTemplate template;

	private Switch fly;

	@Autowired
	private BatteryLevelGauge batteryLevel;

	private ValueChangeListener flyValueChangeListener = new ValueChangeListener() {
		private static final long serialVersionUID = 643887575877284028L;

		@Override
		public void valueChange(ValueChangeEvent event) {
			if (fly.getValue()) {
				template.takeOff();
			} else {
				template.land();
			}
		}
	};


	@Override
	public void afterPropertiesSet() throws Exception {
		layout = new VerticalComponentGroup("Controls");
		layout.setSizeFull();

		fly = new Switch("Fly");
		fly.addValueChangeListener(flyValueChangeListener);

		layout.addComponent(fly);
		layout.addComponent(batteryLevel);

		setCompositionRoot(layout);
	}
}