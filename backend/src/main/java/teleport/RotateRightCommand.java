package teleport;

class RotateRightCommand extends PcmdCommand {
    public RotateRightCommand(int commandSeqNo, float pitch, float roll, float gaz, float yaw) {
        super(commandSeqNo, 0, 0, 0, yaw);
    }

	
	/*@Override
    public byte[] toCommandString(int commandSequenceNumber) {
		return (wrap(intOfFloat(0.5f), commandSequenceNumber) + "\r").getBytes();
	}

	@Override
	protected String wrap(int speed, int commandSequenceNumber) {
		return "AT*PCMD=" + commandSequenceNumber + ",1,0,0,0," + speed;
	}*/
}
