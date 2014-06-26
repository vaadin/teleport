package teleport;

class MoveByAxisCommand extends PcmdCommand {

	public MoveByAxisCommand(int commandSeqNo, float pitch, float roll) {
		super(commandSeqNo, pitch, roll, 0, 0);
	}
}
