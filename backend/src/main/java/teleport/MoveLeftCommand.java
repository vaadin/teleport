package teleport;

class MoveLeftCommand extends PcmdCommand {

	public MoveLeftCommand(int commandSeqNo, float roll) {
        super(commandSeqNo, 0, -1 * roll, 0, 0);
    }
}
