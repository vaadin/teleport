package teleport;

abstract class RefCommand extends DroneCommand {

    private final String commandArgument;

    public RefCommand(int commandSeqNo, String cmd) {
        super(CommandType.REF, commandSeqNo);
        this.commandArgument = cmd;
        
        buildCommand();
    }

    @Override
    protected String buildParameters() {
        return this.commandArgument;
    }
}
