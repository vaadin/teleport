package com.drone.command;

public abstract class DroneCommand {

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

        public String getName() {
            return name;
        }
    }

    private String command;

    private CommandType commandType;
    private int commandSeqNo;

    public DroneCommand(CommandType commandType, int commandSeqNo) {
        this.commandType = commandType;
        this.commandSeqNo = commandSeqNo;

    }

    protected void buildCommand() {
        this.command = "AT*" + commandType.name() + "=" + commandSeqNo + ","
                + buildParameters() + "\r";
    }

    @Override
    public String toString() {
        return this.command;
    }

    protected abstract String buildParameters();
}
