package com.drone.ui;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.UIScope;
import org.vaadin.spring.VaadinComponent;
import org.vaadin.spring.events.EventBus;
import org.vaadin.spring.events.EventBusListenerMethod;

import com.drone.event.DroneBatteryEvent;
import com.drone.ui.charts.BatteryLevelGauge;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;

@VaadinComponent
@UIScope
public class GaugePanel extends CustomComponent implements InitializingBean,
		DisposableBean {
	private static final long serialVersionUID = 4035048387445957610L;

	private BatteryLevelGauge battery;

	private HorizontalLayout layout;

	@Autowired
	private EventBus eventBus;

	public GaugePanel() {
		layout = new HorizontalLayout();
		layout.setWidth(100, Unit.PERCENTAGE);

		battery = new BatteryLevelGauge();
		layout.addComponent(battery);
		layout.setExpandRatio(battery, 1);

		layout.setComponentAlignment(battery, Alignment.TOP_RIGHT);

		addStyleName("gauge-panel");

		setCompositionRoot(layout);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		eventBus.subscribe(this);
	}

	@EventBusListenerMethod
	protected void onBatteryLevelEvent(DroneBatteryEvent event) {
		if (event.getBatteryLevel() < 0) {
			return;
		}

		battery.setBatteryLevel(event.getBatteryLevel());
	}

	@Override
	public void destroy() throws Exception {
		eventBus.unsubscribe(this);
	}
}
