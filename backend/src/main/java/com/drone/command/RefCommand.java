package com.drone.command;

import org.springframework.core.Ordered;

public abstract class RefCommand extends DroneCommand {

    private final String commandArgument;

    public RefCommand(boolean takeoff, boolean emergency) {
        super(CommandType.REF);

        int value = (1 << 18) | (1 << 20) | (1 << 22) | (1 << 24) | (1 << 28);

        if (emergency) {
            value |= (1 << 8);
        }

        if (takeoff) {
            value |= (1 << 9);
        }

        commandArgument = Integer.toString(value);
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
