package com.drone;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.drone.command.ChangeAltitudeCommand;
import com.drone.command.DroneCommand;
import com.drone.command.HoverCommand;
import com.drone.command.LandCommand;
import com.drone.command.MoveByAxisCommand;
import com.drone.command.ResetEmergencyCommand;
import com.drone.command.TakeOffCommand;
import com.drone.event.DroneEmergencyEvent;
import com.drone.event.DroneBatteryEvent;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DroneTemplate implements InitializingBean,
		ApplicationEventPublisherAware,
		ApplicationListener<ApplicationContextEvent> {
	private static final String DEFAULT_IP = "192.168.1.1";
	private static final int DEFAULT_PORT = 5556;

	private static final int DEFAULT_COMMAND_FPS = 10;

	private final AsyncTaskExecutor taskExecutor;
	private final String ip;

	// all of these variables may be read from a thread
	// they won't be updated by more than one
	// client, of course, so no need for synchronization
	private volatile boolean commandRunner;
	private volatile int commandSleep;
	private volatile float gaz, pitch, roll, yaw;
	private volatile float velocityMultiplier;
	private int commandSequenceNo = 100;

	// future of submitted background thread.
	private Future<?> commandFuture;

	// cache this to avoid DNS lookups
	private byte[] ipBytes = new byte[4];

	private ApplicationEventPublisher droneEventPublisher;

	private int droneBattery = 100;

	private final Runnable commandRunnable = () -> {
		while (this.commandRunner) {

			if (isStationary()) {
				executeCommand(new HoverCommand(nextCommandSequenceNumber()));
			} else {
				if (pitch != 0 || roll != 0 || yaw != 0) {
					executeCommand(new MoveByAxisCommand(
							nextCommandSequenceNumber(), pitch, roll, yaw,
							velocityMultiplier));
				}
				if (gaz != 0) {
					executeCommand(new ChangeAltitudeCommand(
							nextCommandSequenceNumber(), gaz));
				}
			}

			if (commandSequenceNo % 200 == 0) {
				droneEventPublisher.publishEvent(new DroneEmergencyEvent(this));
			}
			if (commandSequenceNo % 50 == 0) {
				droneEventPublisher.publishEvent(new DroneBatteryEvent(this, droneBattery--));
			}

			try {
				TimeUnit.MILLISECONDS.sleep(this.commandSleep);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		executeCommand(new LandCommand(nextCommandSequenceNumber()));
	};

	public DroneTemplate(AsyncTaskExecutor taskExecutor) {
		this(null, DEFAULT_COMMAND_FPS, taskExecutor);
	}

	public DroneTemplate() {
		this(new SimpleAsyncTaskExecutor());
	}

	public DroneTemplate(String ip, int fps, AsyncTaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
		Assert.notNull(this.taskExecutor, "you must specify a TaskExecutor!");

		this.ip = StringUtils.hasText(ip) ? ip : DEFAULT_IP;

		setCommandFPS(fps);

		afterPropertiesSet();
	}

	public void setCommandFPS(int fps) {
		commandSleep = 1000 / fps;
	}

	public Future<?> startCommandRunner() {
		commandRunner = true;
		this.commandFuture = this.taskExecutor.submit(this.commandRunnable);
		return this.commandFuture;
	}

	public void stop() {
		commandRunner = false;
		Optional.of(this.commandFuture).ifPresent(future -> {
			if (!(future.isCancelled() || future.isDone())) {
				future.cancel(true);
			}
		});
	}

	protected int nextCommandSequenceNumber() {
		return commandSequenceNo++;
	}

	protected void executeCommand(DroneCommand command) {
		try (DatagramSocket socket = new DatagramSocket()) {
			socket.send(acquireCommandPacket(command));
		} catch (Exception ignored) {
			// NOP since we're sending 10 per second anyway.
		}
	}

	private DatagramPacket acquireCommandPacket(DroneCommand command)
			throws UnknownHostException {
		String stringRepresentation = command.toString();
		byte[] buffer = stringRepresentation.getBytes();

		System.out.println(stringRepresentation);

		return new DatagramPacket(buffer, buffer.length,
				InetAddress.getByAddress(ipBytes), DEFAULT_PORT);
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

	public void setVelocity(double velocity) {
		this.velocityMultiplier = (float) (velocity / 100.0);
	}

	public void takeOff() {
		executeCommand(new TakeOffCommand(nextCommandSequenceNumber()));
	}

	public void land() {
		executeCommand(new LandCommand(nextCommandSequenceNumber()));
	}

	@Override
	public void afterPropertiesSet() {
		StringTokenizer st = new StringTokenizer(ip, ".");

		for (int i = 0; i < 4; i++) {
			ipBytes[i] = (byte) Integer.parseInt(st.nextToken());
		}
	}

	@Override
	public void setApplicationEventPublisher(
			ApplicationEventPublisher applicationEventPublisher) {
		this.droneEventPublisher = applicationEventPublisher;
	}

	@Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		startCommandRunner();
	}

	public void resetEmergency() {
		executeCommand(new ResetEmergencyCommand(nextCommandSequenceNumber()));
	}
}
