package com.drone.command;

public class ConfigCommand extends DroneCommand {
    private String configParam;
    private String value;

    public ConfigCommand(int commandSeqNo, String configParam, String value) {
        super(CommandType.CONFIG, commandSeqNo);
        this.configParam = configParam;

        this.value = value;
    }

    public ConfigCommand(int commandSeqNo, String configParam, long value) {
        this(commandSeqNo, configParam, String.valueOf(value));
    }

    public ConfigCommand(int commandSeqNo, String configParam, double value) {
        this(commandSeqNo, configParam, Double.doubleToLongBits(value));
    }

    public ConfigCommand(int commandSeqNo, String configParam, boolean value) {
        this(commandSeqNo, configParam, String.valueOf(value));
    }

    @Override
    protected String buildParameters() {
        return configParam + "," + value;
    }
}
