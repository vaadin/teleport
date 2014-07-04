package com.drone.command;

public class ConfigCommand extends DroneCommand {
    private String configParam;
    private String value;

    public ConfigCommand(String configParam, String value) {
        super(CommandType.CONFIG);
        this.configParam = configParam;
        this.value = value;

    }

    public ConfigCommand(String configParam, long value) {
        this(configParam, String.valueOf(value));
    }

    public ConfigCommand(String configParam, double value) {
        this(configParam, Double.doubleToLongBits(value));
    }

    public ConfigCommand(String configParam, boolean value) {
        this(configParam, String.valueOf(value));
    }

    private String enquote(String mot) {
        char quote = '"';
        return quote + mot + quote;
    }

    @Override
    protected String buildParameters() {
        return enquote(configParam) + "," + enquote(value);
    }
}
