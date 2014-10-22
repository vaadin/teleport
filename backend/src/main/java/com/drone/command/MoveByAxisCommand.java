package com.drone.command;

public class MoveByAxisCommand extends PcmdCommand {

    public MoveByAxisCommand(float pitch, float roll, float yaw, float gaz,
            float maxSpeed) {
        super(pitch * maxSpeed, roll * maxSpeed, gaz * maxSpeed, yaw * maxSpeed);
    }
}
