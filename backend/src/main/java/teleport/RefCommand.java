package teleport;

abstract class RefCommand extends DroneCommand {

    private final String commandArgument;

    public RefCommand(int commandSeqNo, String cmd) {
        super(CommandType.REF, commandSeqNo);
        this.commandArgument = cmd;
    }

    @Override
    protected String buildCommand() {
        return this.commandArgument;
    }
}
