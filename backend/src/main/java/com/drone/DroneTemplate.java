package com.drone;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
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

public class DroneTemplate implements InitializingBean, DisposableBean {
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

    private List<DroneStateChangeCallback> stateChangeCallbacks = new ArrayList<>();

    private DatagramSocket commandSocket;
    private DatagramSocket dataSocket;

    private InetAddress address;

    private final Runnable commandRunnable = () -> {

        try {
            while (this.commandRunner) {
                if (!stateChangeCallbacks.isEmpty()) {
                    DroneState droneState = getLatestState();
                    stateChangeCallbacks.forEach(scb -> scb
                            .onDroneStateChanged(droneState));
                }

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

    public DroneTemplate(AsyncTaskExecutor taskExecutor,
            DroneStateChangeCallback... callbacks) throws UnknownHostException {
        this(null, DEFAULT_COMMAND_FPS, taskExecutor, callbacks);
    }

    public DroneTemplate(DroneStateChangeCallback... callbacks)
            throws UnknownHostException {
        this(new SimpleAsyncTaskExecutor(), callbacks);
    }

    public DroneTemplate(String ip, int fps, AsyncTaskExecutor taskExecutor,
            DroneStateChangeCallback... callbacks) throws UnknownHostException {
        this.taskExecutor = taskExecutor;
        Assert.notNull(this.taskExecutor, "you must specify a TaskExecutor!");

        stateChangeCallbacks.addAll(Arrays.asList(callbacks));

        ip = StringUtils.hasText(ip) ? ip : DEFAULT_IP;
        address = buildInetAddress(ip);

        setCommandFPS(fps);
    }

    public void setCommandFPS(int fps) {
        commandSleep = 1000 / fps;
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
            ticklePort(dataSocket, DEFAULT_DATA_PORT);
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
    }

    public void takeOff() {
        executeCommand(new TakeOffCommand(nextCommandSequenceNumber()));
    }

    public void land() {
        executeCommand(new LandCommand(nextCommandSequenceNumber()));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        commandSocket = connect(DEFAULT_PORT, false, 3);
        dataSocket = connect(DEFAULT_DATA_PORT, true, 3);

        commandRunner = true;
        this.taskExecutor.submit(this.commandRunnable);
    }

    private InetAddress buildInetAddress(String ip) throws UnknownHostException {
        StringTokenizer st = new StringTokenizer(ip, ".");

        byte[] ipBytes = new byte[4];

        for (int i = 0; i < 4; i++) {
            ipBytes[i] = (byte) Integer.parseInt(st.nextToken());
        }

        return InetAddress.getByAddress(ipBytes);
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
        stop();

        if (commandSocket != null) {
            commandSocket.close();
        }
        if (dataSocket != null) {
            dataSocket.close();
        }
    }
}
