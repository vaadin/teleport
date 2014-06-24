package teleport;

public class MoveBackwardsCommand extends PCMDCommand {
	private static final String BACK = "1045220557";

	@Override
	public byte[] asBytes(int commandSequenceNumber) {
		return wrap(BACK, commandSequenceNumber).getBytes();
	}

	@Override
	protected String wrap(String command, int commandSequenceNumber) {
		return "AT*PCMD=" + commandSequenceNumber + ",1,0," + command + " ,0,0";
	}
}
