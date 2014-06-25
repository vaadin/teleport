package teleport;

class MoveRightCommand extends PcmdCommand {
  
	public MoveRightCommand(int commandSeqNo, float roll) {
        super(commandSeqNo, roll, 0, 0, 0);
    }
}
