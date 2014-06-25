package teleport;

import org.vaadin.spring.VaadinUI;

import com.vaadin.annotations.Theme;
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
	};

	@Override
	protected void init(VaadinRequest request) {
		setSizeFull();

		Button takeoff = new Button("Takeoff");
		takeoff.addClickListener(e -> serviceProvider.takeOff());

		Button land = new Button("Land");
		land.addClickListener(e -> serviceProvider.land());

		HorizontalLayout basics = new HorizontalLayout();
		basics.addComponents(takeoff, land);

		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);

		UpDownTurnLeftRight tlr = new UpDownTurnLeftRight(serviceProvider);
		ForwardBackwardLeftRight udlr = new ForwardBackwardLeftRight(
				serviceProvider);

		layout.addComponents(basics, tlr, udlr);

		layout.setComponentAlignment(udlr, Alignment.TOP_RIGHT);

		setContent(layout);
	}
}
