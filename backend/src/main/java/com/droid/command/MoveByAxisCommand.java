package com.droid.command;

public class MoveByAxisCommand extends PcmdCommand {

	public MoveByAxisCommand(int commandSeqNo, float pitch, float roll, float yaw) {
		super(commandSeqNo, pitch, roll, 0, yaw);
	}
}
