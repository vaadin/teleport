package teleport;

class LandCommand extends RefCommand {

    private static final String LANDING_COMMAND = "290717696";

    public LandCommand(int commandSeqNo) {
        super(commandSeqNo, LANDING_COMMAND);
    }
}
