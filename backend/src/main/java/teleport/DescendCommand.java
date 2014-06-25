package teleport;

class DescendCommand extends PcmdCommand {
    public DescendCommand(int commandSeqNo, float gaz) {
        super(commandSeqNo, 0, 0, -1 * gaz, 0);
    }
}
