package com.drone.command;

public abstract class RefCommand extends DroneCommand {

    private final String commandArgument;

    public RefCommand(String cmd) {
        super(CommandType.REF);
        this.commandArgument = cmd;
    }

    @Override
    protected String buildParameters() {
        return this.commandArgument;
    }
}
