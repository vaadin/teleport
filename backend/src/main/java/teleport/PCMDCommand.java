package teleport;

public abstract class PCMDCommand implements DroneCommand {

	protected abstract String wrap(String command, int commandSequenceNumber);
}
