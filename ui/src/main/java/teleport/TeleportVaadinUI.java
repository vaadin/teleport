package teleport;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.spring.VaadinUI;

import teleport.DroneService;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
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
	
	private DroneServiceProvider serviceProvider = new DroneServiceProvider() {
		
		@Override
		public void up() {
			service.executeCommand(new MoveUpCommand());
		}
		
		@Override
		public void rotateRight() {
			service.executeCommand(new RotateRightCommand());
		}
		
		@Override
		public void rotateLeft() {
			service.executeCommand(new RotateLeftCommand());
		}
		
		@Override
		public void right() {
			service.executeCommand(new MoveRightCommand());
		}
		
		@Override
		public void left() {
			service.executeCommand(new MoveLeftCommand());
		}
		
		@Override
		public void down() {
			service.executeCommand(new MoveDownCommand());
		}

		@Override
		public void takeOff() {
			service.executeCommand(new TakeOffCommand());
		}

		@Override
		public void land() {
			service.executeCommand(new LandCommand());
		}

		@Override
		public void forward() {
			service.executeCommand(new MoveForwardCommand());
		}

		@Override
		public void backward() {
			service.executeCommand(new MoveBackwardsCommand());
		}
	};

	@Override
	protected void init(VaadinRequest request) {
		setSizeFull();
		
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		UpDownTurnLeftRight tlr = new UpDownTurnLeftRight(serviceProvider);
		ForwardBackwardLeftRight udlr = new ForwardBackwardLeftRight(serviceProvider);
		
		layout.addComponents(tlr, udlr);
		
		layout.setComponentAlignment(udlr, Alignment.TOP_RIGHT);
		
		setContent(layout);
	}
}
