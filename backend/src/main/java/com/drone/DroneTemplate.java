package com.drone;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Optional;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
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
import com.drone.event.DroneControlUpdateEvent;
import com.drone.event.DroneEmergencyEvent;
import com.drone.event.DroneLowBatteryEvent;
import com.drone.event.property.DroneAltitudeEvent;
import com.drone.event.property.DroneBatteryEvent;

public class DroneTemplate implements InitializingBean, DisposableBean,
        ApplicationEventPublisherAware,
        ApplicationListener<ApplicationContextEvent> {
    private static final String DEFAULT_IP = "192.168.1.1";
    private static final int DEFAULT_PORT = 5556;
    private static final int DEFAULT_DATA_PORT = 5554;

    private static final int DEFAULT_COMMAND_FPS = 10;

    private final AsyncTaskExecutor taskExecutor;

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

    private Random random = new Random();

    private ApplicationEventPublisher droneEventPublisher;

    private float droneBattery = 1;

    private DatagramSocket commandSocket;
    private DatagramSocket dataSocket;

    private InetAddress address;

    private final Runnable commandRunnable = () -> {

        try {
            while (this.commandRunner) {

                DroneState droneState = getLatestState();

                try {
                    if (isStationary()) {
                        executeCommand(new HoverCommand(
                                nextCommandSequenceNumber()));
                    } else {
                        if (pitch != 0 || roll != 0 || yaw != 0) {
                            executeCommand(new MoveByAxisCommand(
                                    nextCommandSequenceNumber(), pitch, roll,
                                    yaw, velocityMultiplier));
                        }
                        if (gaz != 0) {
                            executeCommand(new ChangeAltitudeCommand(
                                    nextCommandSequenceNumber(), gaz));
                        }
                    }

                    if (commandSequenceNo % 20 == 0) {
                        produceEmergencyEventIfNecessary(droneState);
                        produceLowBatteryEventIfNecessary(droneState);
                    }
                    if (commandSequenceNo % 10 == 0) {
                        droneEventPublisher.publishEvent(new DroneBatteryEvent(
                                this, droneBattery -= 0.01f));
                    }

                    if (commandSequenceNo % 10 == 0) {
                        droneEventPublisher
                                .publishEvent(new DroneAltitudeEvent(this,
                                        (float) random.nextDouble()));
                    }

                    TimeUnit.MILLISECONDS.sleep(this.commandSleep);
                } catch (InterruptedException ignored) {

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executeCommand(new LandCommand(nextCommandSequenceNumber()));
        }
    };

    public DroneTemplate(AsyncTaskExecutor taskExecutor)
            throws UnknownHostException {
        this(null, DEFAULT_COMMAND_FPS, taskExecutor);
    }

    private void produceEmergencyEventIfNecessary(DroneState droneState) {
        if (droneState.isEmergency()) {
            droneEventPublisher.publishEvent(new DroneEmergencyEvent(this));
        }
    }

    private void produceLowBatteryEventIfNecessary(DroneState droneState) {
        if (droneState.isBatteryTooLow()) {
            droneEventPublisher.publishEvent(new DroneLowBatteryEvent(this));
        }
    }

    public DroneTemplate() throws UnknownHostException {
        this(new SimpleAsyncTaskExecutor());
    }

    public DroneTemplate(String ip, int fps, AsyncTaskExecutor taskExecutor)
            throws UnknownHostException {
        this.taskExecutor = taskExecutor;
        Assert.notNull(this.taskExecutor, "you must specify a TaskExecutor!");

        ip = StringUtils.hasText(ip) ? ip : DEFAULT_IP;
        address = buildInetAddress(ip);

        setCommandFPS(fps);
    }

    public void setCommandFPS(int fps) {
        commandSleep = 1000 / fps;
    }

    public Future<?> startCommandRunner() {
        commandRunner = true;
        this.commandFuture = this.taskExecutor.submit(this.commandRunnable);
        return this.commandFuture;
    }

    private DatagramSocket connect(int port, boolean tickle, int timeoutSeconds)
            throws Exception {
        DatagramSocket ds = new DatagramSocket(port);
        ds.setSoTimeout((int) TimeUnit.SECONDS.toMillis(timeoutSeconds));
        // Assert.isTrue(ds.isConnected(), "the socket must be connected");

        if (tickle) {
            ticklePort(ds, port);
        }

        return ds;
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
        try {
            commandSocket.send(acquireCommandPacket(command));
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute command", e);
        }
    }

    protected void ticklePort(DatagramSocket socket, int port)
            throws UnknownHostException {
        byte[] buf = { 0x01, 0x00, 0x00, 0x00 };
        DatagramPacket packet = new DatagramPacket(buf, buf.length, address,
                port);
        try {
            if (socket != null) {
                socket.send(packet);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DroneState getLatestState() {
        try {
            DatagramPacket packet = new DatagramPacket(new byte[2048], 2048,
                    address, DEFAULT_DATA_PORT);
            dataSocket.receive(packet);

            ByteBuffer buffer = ByteBuffer.wrap(packet.getData(), 0,
                    packet.getLength());

            return parseDroneState(buffer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to get latest state", e);
        }
    }

    private DatagramPacket acquireCommandPacket(DroneCommand command)
            throws UnknownHostException {
        String stringRepresentation = command.toString();
        byte[] buffer = stringRepresentation.getBytes();

        System.out.println(stringRepresentation);

        return new DatagramPacket(buffer, buffer.length, address, DEFAULT_PORT);
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
        droneEventPublisher.publishEvent(new DroneControlUpdateEvent(this));
    }

    public void takeOff() {
        executeCommand(new TakeOffCommand(nextCommandSequenceNumber()));
        droneEventPublisher.publishEvent(new DroneControlUpdateEvent(this));
    }

    public void land() {
        executeCommand(new LandCommand(nextCommandSequenceNumber()));
        droneEventPublisher.publishEvent(new DroneControlUpdateEvent(this));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        commandSocket = connect(DEFAULT_PORT, false, 3);
        dataSocket = connect(DEFAULT_DATA_PORT, true, 3);

        startCommandRunner();
    }

    private InetAddress buildInetAddress(String ip) throws UnknownHostException {
        StringTokenizer st = new StringTokenizer(ip, ".");

        byte[] ipBytes = new byte[4];

        for (int i = 0; i < 4; i++) {
            ipBytes[i] = (byte) Integer.parseInt(st.nextToken());
        }

        return InetAddress.getByAddress(ipBytes);
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

    private DroneState parseDroneState(ByteBuffer b) {
        b.order(ByteOrder.LITTLE_ENDIAN);
        int magic = b.getInt();
        int state = b.getInt();
        int vision = b.getInt();

        return new DroneState(state, vision);
    }

    @Override
    public void destroy() throws Exception {
        if (commandSocket != null) {
            commandSocket.close();
        }
        if (dataSocket != null) {
            dataSocket.close();
        }
    }
}
