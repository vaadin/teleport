package com.drone.command;

import org.springframework.core.Ordered;

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

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
