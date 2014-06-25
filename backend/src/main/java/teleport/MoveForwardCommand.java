package teleport;

class MoveForwardCommand extends PcmdCommand {

	public MoveForwardCommand(int commandSeqNo, float speed) {
		super(commandSeqNo, speed, 0, 0, 0);
	}
}
