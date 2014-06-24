package teleport;

import org.vaadin.spring.VaadinUI;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@VaadinUI
@Theme("dawn")
public class TeleportVaadinUI extends UI {
	private static final long serialVersionUID = 6337889226477810842L;

	@Override
	protected void init(VaadinRequest request) {
		VerticalLayout layout = new VerticalLayout();
		Label hello = new Label("Hello!");
		layout.addComponent(hello);
		
		setContent(layout);
	}
}
