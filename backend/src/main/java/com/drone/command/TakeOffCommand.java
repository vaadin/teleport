package com.drone.command;

public class TakeOffCommand extends RefCommand {

    private static final String TAKEOFF_COMMAND = "290718208";

    public TakeOffCommand() {
        super(TAKEOFF_COMMAND);
    }
}
