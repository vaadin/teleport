package teleport;

public class LandCommand extends RefCommand {
	private static final String LANDING_COMMAND = "290717696";
	
	@Override
	public byte[] asBytes(int commandSequenceNumber) {
		return asBytes(LANDING_COMMAND, commandSequenceNumber);
	}

}
