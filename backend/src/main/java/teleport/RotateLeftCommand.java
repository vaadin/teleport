package teleport;

class RotateLeftCommand extends PcmdCommand {
    public RotateLeftCommand(int commandSeqNo, float yaw) {
        super(commandSeqNo, 0, 0, 0, -1 * yaw);
    }
}
