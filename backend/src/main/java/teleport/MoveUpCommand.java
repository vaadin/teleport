package teleport;

public class MoveUpCommand extends PCMDCommand {

	private final static String UP = "1045220557";
	
	@Override
	public byte[] asBytes(int commandSequenceNumber) {
		return wrap(UP, commandSequenceNumber).getBytes();
	}

	@Override
	protected String wrap(String command, int commandSequenceNumber) {
		return "AT*PCMD=" + commandSequenceNumber + ",1,0,0," + UP + ",0";
	}

}
