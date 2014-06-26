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

@VaadinUI
@Theme("dawn")
public class TeleportVaadinUI extends UI {
	private static final long serialVersionUID = 6337889226477810842L;

	private DroneTemplate service = new DroneTemplate();

	private DroneServiceProvider serviceProvider = new DroneServiceProvider() {

		@Override
		public void up() {
			service.ascend(0.5f);
		}

		@Override
		public void rotateRight() {
			service.rotateRight(0.5f);
		}

		@Override
		public void rotateLeft() {
			service.rotateLeft(0.5f);
		}

		@Override
		public void right() {
			service.moveRight(0.5f);
		}

		@Override
		public void left() {
			service.moveLeft(0.5f);
		}

		@Override
		public void down() {
			service.descend(0.5f);
		}

		@Override
		public void takeOff() {
			service.takeOff();
		}

		@Override
		public void land() {
			service.land();
		}

		@Override
		public void forward() {
			service.moveForward(0.5f);
		}

		@Override
		public void backward() {
			service.moveBackwards(0.5f);
		}

		@Override
		public void moveByAxis(float pitch, float roll) {
			service.moveByAxis(pitch, roll);
		}
	};

	@Override
	protected void init(VaadinRequest request) {
		setSizeFull();

		Button takeoff = new Button("Takeoff");
		takeoff.addClickListener(e -> serviceProvider.takeOff());

		Button land = new Button("Land");
		land.addClickListener(e -> serviceProvider.land());
		land.setClickShortcut(KeyCode.SPACEBAR);

		HorizontalLayout basics = new HorizontalLayout();
		basics.addComponents(takeoff, land);

		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		UpDownTurnLeftRight tlr = new UpDownTurnLeftRight(serviceProvider);

		JogDial movement = new JogDial();
		movement.addAxisMoveListener(new AxisMoveListener() {
			private static final long serialVersionUID = -4259633131223375918L;

			@Override
			public void onAxisMoved(AxisMoveEvent event) {
				serviceProvider.moveByAxis(event.getX(), event.getY());
			}
		});

		layout.addComponents(basics, tlr, movement);

		layout.setComponentAlignment(movement, Alignment.TOP_RIGHT);

		setContent(layout);
	}
}
