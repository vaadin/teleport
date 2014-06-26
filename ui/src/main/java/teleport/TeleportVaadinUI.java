package teleport;

import org.vaadin.spring.VaadinUI;

import com.vaadin.annotations.Theme;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.jogdial.JogDial;
import com.vaadin.jogdial.JogDial.AxisMoveEvent;
import com.vaadin.jogdial.JogDial.AxisMoveListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@VaadinUI
@Theme("dawn")
public class TeleportVaadinUI extends UI {
	private static final long serialVersionUID = 6337889226477810842L;

	private DroneTemplate service = new DroneTemplate();

	private DroneServiceProvider serviceProvider = new DroneServiceProvider() {

		@Override
		public void takeOff() {
			service.takeOff();
		}

		@Override
		public void land() {
			service.land();
		}

		@Override
		public void moveByAxis(float pitch, float roll) {
			service.moveByAxis(pitch, roll);
		}

		@Override
		public void rotateByAxis(float x) {
			service.rotate(x);
		}

		@Override
		public void changeAltitudeByAxis(float y) {
			service.changeAltitude(y);
		}
	};

	@Override
	protected void init(VaadinRequest request) {
		setSizeFull();

		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);

		setContent(mainLayout);

		Button takeoff = new Button("Takeoff");
		takeoff.addClickListener(e -> serviceProvider.takeOff());

		Button land = new Button("Land");
		land.addClickListener(e -> serviceProvider.land());
		land.setClickShortcut(KeyCode.SPACEBAR);

		HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);

		buttonLayout.addComponents(takeoff, land);

		JogDial rotation = new JogDial();
		rotation.addAxisMoveListener(e -> serviceProvider.rotateByAxis(e.getX()));
//		rotation.addAxisMoveListener(e -> serviceProvider
//				.changeAltitudeByAxis(e.getY()));

		JogDial movement = new JogDial();
		movement.addAxisMoveListener(e -> serviceProvider.moveByAxis(e.getX(),
				e.getY()));

		HorizontalLayout jogDialLayout = new HorizontalLayout();
		jogDialLayout.setWidth(100, Unit.PERCENTAGE);
		jogDialLayout.addComponents(rotation, movement);
		jogDialLayout.setExpandRatio(movement, 1);
		jogDialLayout.setComponentAlignment(movement, Alignment.BOTTOM_RIGHT);

		mainLayout.addComponents(buttonLayout, jogDialLayout);
		mainLayout.setExpandRatio(jogDialLayout, 1);
		mainLayout.setComponentAlignment(jogDialLayout, Alignment.BOTTOM_LEFT);
	}
}
