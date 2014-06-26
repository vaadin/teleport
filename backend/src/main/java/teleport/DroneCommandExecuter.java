package teleport;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

public class DroneCommandExecuter implements Runnable {
	private static final String DEFAULT_IP = "192.168.1.1";
	private static final int DEFAULT_PORT = 5556;
	private static final int DEFAULT_COMMAND_FPS = 5;

	private int commandSequenceNo = 100;

	private Thread thread;
	private boolean running;

	private byte[] ipBytes = new byte[4];

	private float pitch, roll, yaw;

	private int sleep;
	private float gaz;

	public DroneCommandExecuter() {
		setCommandFPS(DEFAULT_COMMAND_FPS);
		initializeIpBytes(DEFAULT_IP);

		thread = new Thread(this);
	}

	public void setCommandFPS(int fps) {
		sleep = 1000 / fps;
	}

	public void start() {
		running = true;
		thread.start();
	}

	public void stop() {
		running = false;
	}

	@Override
	public void run() {
		while (isRunning()) {
			try {
				if (isStationary()) {
					executeCommand(new HoverCommand(commandSequenceNo++));
					setCommandFPS(1);
				} else {
					setCommandFPS(5);
					if (pitch != 0 || roll != 0) {
						executeCommand(new MoveByAxisCommand(
								commandSequenceNo++, pitch, roll));
					}
					if (yaw != 0) {
						executeCommand(new RotateByAxisCommand(
								commandSequenceNo++, yaw));
					}
					if (gaz != 0) {
						executeCommand(new ChangeAltitudeCommand(
								commandSequenceNo++, gaz));
					}
				}
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
			} 
		}
		
		executeCommand(new LandCommand(commandSequenceNo++));
	}

	private boolean isRunning() {
		return running;
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

	private void initializeIpBytes(String droneIP) {
		StringTokenizer st = new StringTokenizer(droneIP, ".");

		for (int i = 0; i < 4; i++) {
			ipBytes[i] = (byte) Integer.parseInt(st.nextToken());
		}
	}

	private DatagramPacket acquireCommandPacket(DroneCommand command)
			throws UnknownHostException {
		String stringRepresentation = command.toString();
		System.out.println(stringRepresentation);
		byte[] buffer = stringRepresentation.getBytes();

		DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
				InetAddress.getByAddress(ipBytes), DEFAULT_PORT);

		return packet;
	}

	private boolean isStationary() {
		return yaw == 0 && gaz == 0 && pitch == 0 && roll == 0;
	}

	public void rotateByAxis(float yaw) {
		this.yaw = yaw;
	}

	public void changeAltitude(float gaz) {
		this.gaz = gaz;
	}

	public void moveByAxis(float pitch, float roll) {
		this.pitch = pitch;
		this.roll = roll;
	}

	public void takeOff() {
		executeCommand(new TakeOffCommand(commandSequenceNo++));
	}

	public void land() {
		executeCommand(new LandCommand(commandSequenceNo++));
	}
}
