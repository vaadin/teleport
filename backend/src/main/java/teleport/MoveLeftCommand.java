package teleport;

public class MoveLeftCommand extends PCMDCommand {
	
	@Override
	public byte[] asBytes(int commandSequenceNumber) {
		return wrap(intOfFloat(-0.5f), commandSequenceNumber).getBytes();
	}

	@Override
	protected String wrap(int speed, int commandSequenceNumber) {
		return "AT*PCMD=" + commandSequenceNumber + ",1,0," + speed + ",0,0";
	}
}
