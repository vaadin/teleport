package com.drone.command;

public class ResetEmergencyCommand extends RefCommand {

	private static final String RESET_EMERGENCY = "";

	public ResetEmergencyCommand(int commandSeqNo) {
		super(commandSeqNo, RESET_EMERGENCY);
	}
}
