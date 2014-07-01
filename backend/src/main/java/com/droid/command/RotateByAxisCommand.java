package com.droid.command;

public class RotateByAxisCommand extends PcmdCommand {
  
	public RotateByAxisCommand(int commandSeqNo, float yaw) {
        super(commandSeqNo, 0, 0, 0, yaw);
    }
}
