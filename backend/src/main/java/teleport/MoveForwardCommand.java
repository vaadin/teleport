package teleport;

public class MoveForwardCommand extends PCMDCommand {
	private static final String FORWARD = "1102263091";
	
	@Override
	public byte[] asBytes(int commandSequenceNumber) {
		return wrap(FORWARD, commandSequenceNumber).getBytes();
	}

	@Override
	protected String wrap(String command, int commandSequenceNumber) {
		return "AT*PCMD=" + commandSequenceNumber + ",1,0," + command + " ,0,0";
	}
}
