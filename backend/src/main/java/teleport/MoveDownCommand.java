package teleport;

public class MoveDownCommand extends PCMDCommand {
	private static String DOWN = "1102263091";
	
	@Override
	public byte[] asBytes(int commandSequenceNumber) {
	return	wrap(DOWN, commandSequenceNumber).getBytes();
	}

	@Override
	protected String wrap(String command, int commandSequenceNumber) {
		return "AT*PCMD=" + commandSequenceNumber + ",1,0,0," + DOWN + ",0";
	}
}
