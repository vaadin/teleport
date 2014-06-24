package teleport;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.VaadinUI;

import teleport.DroneService;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;

@VaadinUI
@Theme("dawn")
public class TeleportVaadinUI extends UI {
	private static final long serialVersionUID = 6337889226477810842L;

	@Autowired
	private DroneService service;
	
	@Override
	protected void init(VaadinRequest request) {
		VerticalLayout layout = new VerticalLayout();
		
		Button takeoff = new Button("Takeoff", new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				service.takeOff();
			}
		});
		
		Button land = new Button("Land", new Button.ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				service.land();
			}
		});
		
		
		layout.addComponents(takeoff, land);


		setContent(layout);
	}
}
