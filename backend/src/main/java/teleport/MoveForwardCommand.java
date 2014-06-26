package teleport;

class MoveForwardCommand extends PcmdCommand {

	public MoveForwardCommand(int commandSeqNo, float roll) {
		super(commandSeqNo, 0, -1 * roll, 0, 0);
	}
}
