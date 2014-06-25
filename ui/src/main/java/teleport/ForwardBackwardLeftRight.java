package teleport;

import org.springframework.stereotype.Component;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

public class ForwardBackwardLeftRight extends CustomComponent {
	private static final long serialVersionUID = -800124043322568340L;

	public ForwardBackwardLeftRight(DroneServiceProvider serviceProvider) {
		VerticalLayout layout = new VerticalLayout();
		layout.setSpacing(true);

		Button forward = new Button(FontAwesome.ARROW_UP);
		forward.addClickListener(e -> serviceProvider.forward());
		forward.setClickShortcut(KeyCode.ARROW_UP);

		Button left = new Button(FontAwesome.ARROW_LEFT);
		left.addClickListener(e -> serviceProvider.left());
		left.setClickShortcut(KeyCode.ARROW_LEFT);

		Button backward = new Button(FontAwesome.ARROW_DOWN);
		backward.addClickListener(e -> serviceProvider.backward());
		backward.setClickShortcut(KeyCode.ARROW_DOWN);

		Button right = new Button(FontAwesome.ARROW_RIGHT);
		right.addClickListener(e -> serviceProvider.right());
		right.setClickShortcut(KeyCode.ARROW_RIGHT);

		HorizontalLayout bottomLayout = new HorizontalLayout();
		bottomLayout.setSpacing(true);
		bottomLayout.addComponents(left, backward, right);

		layout.addComponent(forward);
		layout.addComponent(bottomLayout);

		layout.setComponentAlignment(forward, Alignment.TOP_CENTER);

		setCompositionRoot(layout);
	}
}
