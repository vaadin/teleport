package teleport;

class RotateCommand extends PcmdCommand {
  
	public RotateCommand(int commandSeqNo, float yaw) {
        super(commandSeqNo, 0, 0, 0, yaw);
    }
}
