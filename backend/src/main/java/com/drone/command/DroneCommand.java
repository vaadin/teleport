package com.drone.command;

import org.springframework.core.Ordered;

public abstract class DroneCommand implements Ordered {

    public static enum CommandType {
        REF,
        PCMD,
        CONFIG,
        CONTROL("CTRL"),
        RESET_WATCHDOG("COMWDG");

        private String name;

        private CommandType() {
            this.name = name();
        }

        private CommandType(String name) {
            this.name = name;
        }
    }

    private CommandType commandType;

    private int order;
    public DroneCommand(CommandType commandType) {
        this(commandType, 0);
    }

    public boolean needControlAck() {
        return false;
    }

    public boolean isRepeated() {
        return false;
    }


    protected DroneCommand(CommandType commandType, int order) {
        this.commandType = commandType;
        this.order = order;
    }

    public String buildCommand(int commandSeqNo) {
        return "AT*" + commandType.name() + "=" + commandSeqNo + ","
                + buildParameters() + "\r";
    }


    @Override
    public int getOrder() {
        return this.order;
    }

    protected abstract String buildParameters();
}
