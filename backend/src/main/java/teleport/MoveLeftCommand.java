package teleport;

public class MoveLeftCommand extends PCMDCommand {
	
	@Override
	public byte[] asBytes(int commandSequenceNumber) {
		return wrap(MINUS_POINT_2, commandSequenceNumber).getBytes();
	}

	@Override
	protected String wrap(String command, int commandSequenceNumber) {
		return "AT*PCMD=" + commandSequenceNumber + ",1," + command + ",0,0,0";
	}
}
