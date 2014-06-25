package teleport;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

public class DroneTemplate {

	private static final int DEFAULT_PORT = 5556;
	private static final String DEFAULT_IP = "192.168.1.1";

	private byte[] ipBytes = new byte[4];

	private int commandSequenceNumber = 100;

	public DroneTemplate() {
		this(DEFAULT_IP);
	}

	public DroneTemplate(String droneIP) {
		generateIpBytes(droneIP);
	}

	protected void executeCommand(DroneCommand command) {
		DatagramSocket socket = null;

		try {
			DatagramPacket commandPacket = acquireCommandPacket(command);
			socket = new DatagramSocket();

			socket.send(commandPacket);

		} catch (Exception e) {
			System.err.println("Error sending command");
		} finally {
			if (socket != null) {
				socket.close();
			}
		}
	}

	private void generateIpBytes(String droneIP) {
		StringTokenizer st = new StringTokenizer(droneIP, ".");

		for (int i = 0; i < 4; i++) {
			ipBytes[i] = (byte) Integer.parseInt(st.nextToken());
		}
	}

	private DatagramPacket acquireCommandPacket(DroneCommand command) throws UnknownHostException {
		String stringRepresentation = command.toString();
		System.out.println(stringRepresentation);
		byte[] buffer = stringRepresentation.getBytes();

		DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
				InetAddress.getByAddress(ipBytes), DEFAULT_PORT);

		return packet;
	}

	public void ascend(float f) {
		executeCommand(new AscendCommand(commandSequenceNumber++, f));
	}

	public void descend(float f) {
		executeCommand(new DescendCommand(commandSequenceNumber++, f));
	}

	public void rotateRight(float f) {
		executeCommand(new RotateRightCommand(commandSequenceNumber++, f));
	}

	public void rotateLeft(float f) {
		executeCommand(new RotateLeftCommand(commandSequenceNumber++, f));
	}

	public void moveRight(float f) {
		executeCommand(new MoveRightCommand(commandSequenceNumber++, f));
	}

	public void moveLeft(float f) {
		executeCommand(new MoveLeftCommand(commandSequenceNumber++, f));
	}

	public void takeOff() {
		executeCommand(new TakeOffCommand(commandSequenceNumber++));
	}

	public void land() {
		executeCommand(new LandCommand(commandSequenceNumber++));
	}

	public void moveForward(float f) {
		executeCommand(new MoveForwardCommand(commandSequenceNumber++, f));
	}

	public void moveBackwards(float f) {
		executeCommand(new MoveBackwardsCommand(commandSequenceNumber++, f));
	}
}
