package teleport;

public class MoveForwardCommand extends PCMDCommand {
	
	@Override
	public byte[] asBytes(int commandSequenceNumber) {
		return (wrap(intOfFloat(0.5f), commandSequenceNumber) + "\r").getBytes();
	}

	@Override
	protected String wrap(int speed, int commandSequenceNumber) {
		return "AT*PCMD=" + commandSequenceNumber + ",1," + speed + ",0,0,0";
	}
}
