package teleport;

class MoveRightCommand extends PcmdCommand {
  
	public MoveRightCommand(int commandSeqNo, float roll) {
        super(commandSeqNo, 0, roll, 0, 0);
    }
}
