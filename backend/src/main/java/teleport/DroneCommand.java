package teleport;

public interface  DroneCommand {

	byte[] asBytes(int commandSequenceNumber);

	String name();
}
