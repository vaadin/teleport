package teleport;

class MoveBackwardCommand extends PcmdCommand {

	public MoveBackwardCommand(int commandSeqNo, float roll) {
		super(commandSeqNo, 0, roll, 0, 0);
	}
}
