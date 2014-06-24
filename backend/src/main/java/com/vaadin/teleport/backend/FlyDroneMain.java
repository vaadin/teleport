package com.vaadin.teleport.backend;

import java.io.IOException;


public class FlyDroneMain {

	private static final String TAKE_OFF = "AT*REF=101,290718208\r";
	private static final String LAND = "AT*REF=102,290717696\r";
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		try {
			DroneCommand.executeCommand(TAKE_OFF);
			Thread.sleep(5000);
		}
		finally {
			DroneCommand.executeCommand(LAND);
		}
	}
}
