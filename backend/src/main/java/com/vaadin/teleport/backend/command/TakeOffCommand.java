package com.vaadin.teleport.backend.command;

public class TakeOffCommand extends RefCommand {
	private static final String TAKEOFF_COMMAND = "290718208";

	@Override
	public byte[] asBytes(int commandSequenceNumber) {
		return asBytes(TAKEOFF_COMMAND, commandSequenceNumber);
	}
}
