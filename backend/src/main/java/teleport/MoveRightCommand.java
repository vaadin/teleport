package teleport;

class MoveRightCommand extends PcmdCommand {
  
	public MoveRightCommand(int commandSeqNo, float pitch) {
        super(commandSeqNo, pitch, 0, 0, 0);
    }
}
