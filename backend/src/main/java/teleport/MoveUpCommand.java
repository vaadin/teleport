package teleport;

class MoveUpCommand extends PcmdCommand {
    public MoveUpCommand(int commandSeqNo, float gaz) {
        super(commandSeqNo, 0, 0, gaz, 0);
    }
}
