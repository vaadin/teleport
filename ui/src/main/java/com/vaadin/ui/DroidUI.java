package com.vaadin.ui;

import org.vaadin.spring.VaadinUI;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;

@VaadinUI
@Theme("dawn")
public class DroidUI extends UI {
	private static final long serialVersionUID = 5909107481891537314L;

	@Override
	protected void init(VaadinRequest request) {
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(new Label("Hello from spring test!"));

		setContent(layout);
	}
}
