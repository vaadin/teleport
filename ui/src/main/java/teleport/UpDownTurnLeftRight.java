package teleport;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

public class UpDownTurnLeftRight extends CustomComponent {
	private static final long serialVersionUID = -800124043322568340L;

	public UpDownTurnLeftRight(DroneServiceProvider serviceProvider) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);

		Button up = new Button(FontAwesome.ARROW_CIRCLE_UP);
		up.addClickListener(e -> serviceProvider.up());
		up.setClickShortcut(KeyCode.W);

		Button down = new Button(FontAwesome.ARROW_CIRCLE_DOWN);
		down.addClickListener(e -> serviceProvider.down());
		down.setClickShortcut(KeyCode.S);

		Button left = new Button(FontAwesome.ARROW_CIRCLE_O_LEFT);
		left.addClickListener(e -> serviceProvider.rotateLeft());
		left.setClickShortcut(KeyCode.A);

		Button right = new Button(FontAwesome.ARROW_CIRCLE_O_RIGHT);
		right.addClickListener(e -> serviceProvider.rotateRight());
		right.setClickShortcut(KeyCode.D);

		HorizontalLayout bottomLayout = new HorizontalLayout();
		bottomLayout.setSpacing(true);

		bottomLayout.addComponents(left, down, right);

		layout.addComponents(up, bottomLayout);

		layout.setComponentAlignment(up, Alignment.TOP_CENTER);

		setCompositionRoot(layout);
	}
}
