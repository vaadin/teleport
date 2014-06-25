package teleport;

class MoveForwardCommand extends PcmdCommand {

	public MoveForwardCommand(int commandSeqNo, float speed) {
		super(commandSeqNo, 0, -1 * speed, 0, 0);
	}
}
