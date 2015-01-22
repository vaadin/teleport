package com.drone.command;

public class ControlCommand extends DroneCommand {

    public static enum ControlType {
        NONE,
        ARDRONE_UPDATE,
        PIC_UPDATE,
        LOGS_GET,
        CFG_GET,
        ACK,
        CUSTOM_CFG_GET;
    }

    private ControlType controlType;
    private int value;

    public ControlCommand(ControlType controlType, int value) {
        super(CommandType.CONTROL);
        this.controlType = controlType;
        this.value = value;
    }

    @Override
    protected String buildParameters() {
        return controlType.ordinal() + "," + value;
    }
}
