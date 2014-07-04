package com.drone.command;

public class MoveByAxisCommand extends PcmdCommand {

    public MoveByAxisCommand( float pitch, float roll, float yaw, float maxSpeed) {
        super(  pitch * maxSpeed, roll * maxSpeed, 0, yaw * maxSpeed);
    }
}
