package com.drone.command;

public class MoveByAxisCommand extends PcmdCommand {

	public MoveByAxisCommand(int commandSeqNo, float pitch, float roll,
			float yaw, float maxSpeed) {
		super(commandSeqNo, pitch * maxSpeed, roll * maxSpeed, 0, yaw
				* maxSpeed);
	}
}
