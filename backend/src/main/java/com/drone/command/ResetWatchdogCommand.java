package com.drone.command;

public class ResetWatchdogCommand extends DroneCommand {

    public ResetWatchdogCommand(int commandSeqNo) {
        super(CommandType.RESET_WATCHDOG, commandSeqNo);
        buildCommand();
    }

    @Override
    protected String buildParameters() {
        return "";
    }
}
