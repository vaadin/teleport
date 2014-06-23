package com.vaadin.flydrone;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.StringTokenizer;

public class DroneCommand {

	private static final int DEFAULT_PORT = 5556;
	private static final String DEFAULT_IP = "192.168.1.1";
	
	public static void executeCommand(String command) throws IOException {
		StringTokenizer st = new StringTokenizer(DEFAULT_IP, ".");
		
		byte[] ip_bytes = new byte[4];
		
		for (int i = 0; i < 4; i++) {
			ip_bytes[i] = (byte) Integer.parseInt(st.nextToken());
		}
		
		DatagramSocket socket = new DatagramSocket();
		byte[] buffer = command.getBytes();
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
				InetAddress.getByAddress(ip_bytes), DEFAULT_PORT);
		
		try {
			socket.send(packet);
		}
		finally {
			if(socket != null) {
				socket.close();
			}
		}
	}
}
