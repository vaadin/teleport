package teleport;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import org.springframework.stereotype.Component;

@Component
public class DroneCommandSender {
	private static final int DEFAULT_PORT = 5556;
	private static final String DEFAULT_IP = "192.168.1.1";

	private byte[] ipBytes = new byte[4];

	private int commandSequenceNumber = 100;

	public DroneCommandSender() {
		this(DEFAULT_IP);
	}

	public DroneCommandSender(String droneIP) {
		generateIpBytes(droneIP);
	}

	public void executeCommand(DroneCommand command) {
		DatagramSocket socket = null;

		commandSequenceNumber += 1;

		try {
			DatagramPacket commandPacket = acquireCommandPacket(command,
					commandSequenceNumber);
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

	private DatagramPacket acquireCommandPacket(DroneCommand command,
			int commandSequenceNumber) throws UnknownHostException {
		byte[] buffer = command.asBytes(commandSequenceNumber);
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
				InetAddress.getByAddress(ipBytes), DEFAULT_PORT);

		return packet;
	}
}
