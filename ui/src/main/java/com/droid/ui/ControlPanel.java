package com.droid.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.UIScope;
import org.vaadin.spring.VaadinComponent;

import com.droid.DroneTemplate;
import com.vaadin.addon.touchkit.ui.Switch;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CustomComponent;

@VaadinComponent
@UIScope
public class ControlPanel extends CustomComponent {
	private static final long serialVersionUID = 8067787477217415968L;

	private VerticalComponentGroup layout;
	
	@Autowired
	private DroneTemplate template;
	
	private Switch fly;
	
	private ValueChangeListener flyValueChangeListener = new ValueChangeListener() {
		private static final long serialVersionUID = 643887575877284028L;

		@Override
		public void valueChange(ValueChangeEvent event) {
			if(fly.getValue()) {
				template.takeOff();
			}
			else {
				template.land();
			}
		}
	};
	
	public ControlPanel() {
		layout = new VerticalComponentGroup("Controls");
		layout.setSizeFull();
		
		fly = new Switch("Fly");
		fly.addValueChangeListener(flyValueChangeListener);
		
		layout.addComponent(fly);
		
		setCompositionRoot(layout);
	}
}
