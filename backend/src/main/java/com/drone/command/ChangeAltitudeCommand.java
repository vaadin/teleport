package com.drone.command;

public class ChangeAltitudeCommand extends PcmdCommand {
    public ChangeAltitudeCommand(float gaz) {
        super(0, 0, gaz, 0);
    }
}
