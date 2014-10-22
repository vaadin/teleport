package com.drone.command;

public class RotateByAxisCommand extends PcmdCommand {

    public RotateByAxisCommand(float yaw) {
        super(0, 0, 0, yaw);
    }
}
