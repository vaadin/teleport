package teleport;

class TakeOffCommand extends RefCommand {

    private static final String TAKEOFF_COMMAND = "290718208";

    public TakeOffCommand(int commandSeqNo) {
        super(commandSeqNo, TAKEOFF_COMMAND);
    }

}
