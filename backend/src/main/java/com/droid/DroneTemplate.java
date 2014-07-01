package com.droid;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.droid.command.ChangeAltitudeCommand;
import com.droid.command.DroneCommand;
import com.droid.command.HoverCommand;
import com.droid.command.LandCommand;
import com.droid.command.MoveByAxisCommand;
import com.droid.command.RotateByAxisCommand;
import com.droid.command.TakeOffCommand;
import com.droid.navdata.DroneBinaryNavData;
import com.droid.navdata.DroneNavData;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class DroneTemplate implements InitializingBean {

	private static final String DEFAULT_IP = "192.168.1.1";
	private static final int DEFAULT_PORT = 5556;
	private static final int DEFAULT_COMMAND_FPS = 5;

	private final AsyncTaskExecutor taskExecutor;
	private final String ip;

	// all of these variables may be read from a thread
	// they won't be updated by more than one
	// client, of course, so no need for synchronization
	private volatile boolean running;
	private volatile int sleep;
	private volatile float gaz, pitch, roll, yaw;
	private int commandSequenceNo = 100;

	// future of submitted background thread.
	private Future<?> future;

	// cache this to avoid DNS lookups
	private byte[] ipBytes = new byte[4];

	private final Runnable runnable = () -> {
		while (this.running) {

			if (isStationary()) {
				executeCommand(new HoverCommand(nextCommandSequenceNumber()));
				setCommandFPS(1);
			} else {
				setCommandFPS(5);
				if (pitch != 0 || roll != 0) {
					executeCommand(new MoveByAxisCommand(
							nextCommandSequenceNumber(), pitch, roll));
				}
				if (yaw != 0) {
					executeCommand(new RotateByAxisCommand(
							nextCommandSequenceNumber(), yaw));
				}
				if (gaz != 0) {
					executeCommand(new ChangeAltitudeCommand(
							nextCommandSequenceNumber(), gaz));
				}
			}

			try {
				TimeUnit.MILLISECONDS.sleep(this.sleep);
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
		sleep = 1000 / fps;
	}

	public Future<?> start() {
		running = true;
		this.future = this.taskExecutor.submit(this.runnable);
		return this.future;
	}

	public void stop() {
		running = false;
		Optional.of(this.future).ifPresent(future -> {
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
		} catch (Exception e) {
			throw new RuntimeException(e);
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

	public void takeOff() {
		executeCommand(new TakeOffCommand(nextCommandSequenceNumber()));
	}

	public void land() {
		executeCommand(new LandCommand(nextCommandSequenceNumber()));
	}

	public DroneNavData getLatestNavData() {
		return new DroneBinaryNavData(null);
	}

	@Override
	public void afterPropertiesSet() {
		StringTokenizer st = new StringTokenizer(ip, ".");

		for (int i = 0; i < 4; i++) {
			ipBytes[i] = (byte) Integer.parseInt(st.nextToken());
		}
	}
}
