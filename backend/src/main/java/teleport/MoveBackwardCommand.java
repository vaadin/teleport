package teleport;

class MoveBackwardCommand extends PcmdCommand {

	public MoveBackwardCommand(int commandSeqNo, float speed) {
		super(commandSeqNo, 0, speed, 0, 0);
	}
}
