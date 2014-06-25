package teleport;

abstract class DroneCommand {

    public static enum CommandType {
        REF,
        PCMD
    }

    private String command;

    public DroneCommand(CommandType commandType, int commandSeqNo) {
        this.command = "AT*" + commandType.name() + "=" + commandSeqNo + "," + buildCommand() + "\r";
    }

    @Override
    public String toString() {
        return this.command;
    }

    protected abstract String buildCommand();
}

