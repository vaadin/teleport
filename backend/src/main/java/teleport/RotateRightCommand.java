package teleport;

class RotateRightCommand extends PcmdCommand {
   
	public RotateRightCommand(int commandSeqNo, float yaw) {
        super(commandSeqNo, 0, 0, 0, yaw);
    }
}
