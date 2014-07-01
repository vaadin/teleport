package com.drone.command;

public abstract class DroneCommand {

	public static enum CommandType {
		REF, PCMD
	}

	private String command;

	private CommandType commandType;
	private int commandSeqNo;

	public DroneCommand(CommandType commandType, int commandSeqNo) {
		this.commandType = commandType;
		this.commandSeqNo = commandSeqNo;
	}

	protected void buildCommand() {
		this.command = "AT*" + commandType.name() + "=" + commandSeqNo + ","
				+ buildParameters() + "\r";
	}

	@Override
	public String toString() {
		return this.command;
	}

	protected abstract String buildParameters();
}
