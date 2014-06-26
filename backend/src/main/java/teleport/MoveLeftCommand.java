package teleport;

class MoveLeftCommand extends PcmdCommand {

	public MoveLeftCommand(int commandSeqNo, float pitch) {
        super(commandSeqNo, -1 * pitch, 0, 0, 0);
    }
}
