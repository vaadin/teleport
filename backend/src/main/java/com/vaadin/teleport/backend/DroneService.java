package com.vaadin.teleport.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vaadin.teleport.backend.command.DroneCommand;
import com.vaadin.teleport.backend.command.LandingCommand;
import com.vaadin.teleport.backend.command.TakeOffCommand;

@Service
public class DroneService {

	@Autowired
	private DroneCommandSender commandSender;

	public void executeCommand(DroneCommand command) {
		commandSender.executeCommand(command);
	}

	public void takeOff() {
		commandSender.executeCommand(new TakeOffCommand());
	}

	public void land() {
		commandSender.executeCommand(new LandingCommand());
	}
}
