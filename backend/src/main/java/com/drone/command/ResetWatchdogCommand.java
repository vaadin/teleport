package com.drone.command;

public class ResetWatchdogCommand extends DroneCommand {

    public ResetWatchdogCommand() {
        super(CommandType.RESET_WATCHDOG);
    }

    @Override
    protected String buildParameters() {
        return "";
    }
}
