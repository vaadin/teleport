package com.drone;

import static com.drone.ReaderUtils.getUInt32;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.springframework.context.Lifecycle;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.drone.command.ConfigCommand;
import com.drone.command.ControlCommand;
import com.drone.command.DroneCommand;
import com.drone.command.ResetWatchdogCommand;

public class DroneTemplate2 implements Lifecycle {

    public static final String DEFAULT_IP = "192.168.1.1";

    public static final int COMMAND_PORT = 5556;
    public static final int NAV_PORT = 5554;

    private volatile boolean running = false;
    private final PriorityBlockingQueue<DroneCommand> commands = new PriorityBlockingQueue<>(
            100, (l, r) -> l.getOrder() - r.getOrder());
    private InetAddress address = fromIP(DEFAULT_IP);
    private Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
    private final TaskExecutor taskExecutor;
    private final Object controlAckLock = new Object();
    private final AtomicInteger sequenceNumber = new AtomicInteger(1);
    private final AtomicBoolean controlAck = new AtomicBoolean();

    public static interface ExceptionFriendlyRunnable {
        void run() throws Exception;
    }

    public DroneTemplate2(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void start() {
        this.running = true;

        try {
            init(this.address, COMMAND_PORT, NAV_PORT);
        } catch (Throwable throwable) {
            logger.warn("Ooops!", throwable);
        }
    }

    @Override
    public void stop() {
        this.running = false;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    private DroneState readDroneState(DatagramSocket datagramSocket,
                                      int maxPacketSize) throws IOException {

        DatagramPacket packet = new DatagramPacket(new byte[maxPacketSize],
                maxPacketSize);
        datagramSocket.receive(packet);

        ByteBuffer buffer = ByteBuffer.wrap(packet.getData(), 0,
                packet.getLength());
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        int header = buffer.getInt();
        int state = buffer.getInt();
        long sequence = getUInt32(buffer);
        int vision = buffer.getInt();
        DroneState droneState = new DroneState(sequence, state, vision);
        logger.info("droneState: " + droneState);

        if (droneState.isNavDataDemoOnly()) {
            while (buffer.position() < buffer.limit()) {
                int tag = buffer.getShort() & 0xFFFF;
                int payloadSize = (buffer.getShort() & 0xFFFF) - 4;
                ByteBuffer optionData = buffer.slice().order(
                        ByteOrder.LITTLE_ENDIAN);
                payloadSize = Math.min(payloadSize, optionData.remaining());
                optionData.limit(payloadSize);
                logger.info("Parsin tag " + tag);
                parseOption(tag, optionData);
                buffer.position(buffer.position() + payloadSize);
            }
        }

        return droneState;
    }

    private void parseOption(int tag, ByteBuffer optionData) {
        if (tag == 0) {
            int parsedTag = optionData.getShort();
            int parsedSize = optionData.getShort();
            int battery = optionData.getInt();
            float theta = optionData.getFloat();
            float phi = optionData.getFloat();
            float psi = optionData.getFloat();
            int altitude = optionData.getInt();

            System.out.println("tag:  " + parsedTag + " size: " + parsedSize
                    + " battery: " + battery + " theta: " + theta + " phi: "
                    + phi + " psi: " + psi + " altitude: " + altitude);

        }
    }

    private void enqueueCommand(DroneCommand command) {
        commands.add(command);
    }

    private void bootstrapNavData(DroneState droneState) throws IOException {
        if (droneState.isNavDataBootstrap()) {
            controlAck.set(droneState.isControlReceived()); // todo what's this
            // buy us? Why?
            enqueueCommand(new ConfigCommand("general:navdata_demo", true));
            enqueueCommand(new ControlCommand(ControlCommand.ControlType.ACK, 0));
        }
    }

    private void navDataLoop(InetAddress address, DatagramSocket navDataSocket,
                             int navPort, int maxPacketSize) throws Exception {
        navDataSocket.setSoTimeout(3000);

        // tickle!
        navDataSocket.send(new DatagramPacket(new byte[] { 0x01, 0x00, 0x00,
                0x00 }, 4, address, navPort));

        // get the deon
        DroneState droneState = readDroneState(navDataSocket, maxPacketSize);
        bootstrapNavData(droneState);

        while (isRunning()) {
            droneState = readDroneState(navDataSocket, maxPacketSize);

        }
    }

    private void commandLoop(InetAddress address, DatagramSocket socket,
                             int commandPort) throws Exception {
        DroneCommand ack = new ControlCommand(ControlCommand.ControlType.ACK, 0);
        DroneCommand keepAlive = new ResetWatchdogCommand();
        DroneCommand command = null, stickyCommand = null;
        long lastRecordedTime = 0;
        while (isRunning()) {
            long deltaTime = 0;
            deltaTime = stickyCommand == null ? 40 : System.currentTimeMillis()
                    - lastRecordedTime;
            command = commands.poll(deltaTime, TimeUnit.MILLISECONDS);
            if (command == null) {
                if (stickyCommand == null) {
                    command = keepAlive;
                } else {
                    command = stickyCommand;
                    lastRecordedTime = System.currentTimeMillis();
                }
            } else {
                if (command.isRepeated()) {
                    // sticky commands replace previous sticky
                    stickyCommand = command;
                    lastRecordedTime = System.currentTimeMillis();
                } else /* if (command.clearSticky()) */{
                    // only some commands can clear sticky commands
                    stickyCommand = null;
                }
            }

            DroneCommand gross = command;
            if (gross.needControlAck()) {
                waitForControlAckToBe(false,
                        () -> sendCommand(address, socket, gross, commandPort));
                waitForControlAckToBe(true,
                        () -> sendCommand(address, socket, gross, commandPort));
            } else {
                sendCommand(address, socket, gross, commandPort);
            }
        }
    }

    private void sendCommand(InetAddress address,
                             DatagramSocket datagramSocket, DroneCommand droneCommand,
                             int commandPort) throws IOException {
        Assert.notNull(droneCommand, "the droneCommand must not be null");
        byte[] commandBytes = droneCommand.buildCommand(
                this.sequenceNumber.getAndIncrement()).getBytes();
        datagramSocket.send(new DatagramPacket(commandBytes,
                commandBytes.length, address, commandPort));
    }

    private void waitForControlAckToBe(boolean b,
                                       ExceptionFriendlyRunnable andThen) throws InterruptedException {
        if (controlAck.get() != b) {
            boolean tried = false;
            synchronized (controlAckLock) {
                while (!tried && controlAck.get() != b) {
                    controlAckLock.wait(50);
                    tried = true;
                }
            }
            if (tried && controlAck.get() != b) {
                throw new RuntimeException("control ack timeout "
                        + String.valueOf(b));
            }

            try {
                andThen.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void init(InetAddress address, int commandPort, int navPort)
            throws Throwable {

        int timeout = 3000;

        DatagramSocket commandSocket = new DatagramSocket(commandPort);
        DatagramSocket navSocket = new DatagramSocket(navPort);

        Arrays.asList(commandSocket, navSocket).forEach(s -> {
            try {
                s.setSoTimeout(timeout);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        });

        this.taskExecutor.execute(() -> {
            try {
                commandLoop(address, commandSocket, commandPort);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        this.taskExecutor.execute(() -> {
            try {
                navDataLoop(address, navSocket, navPort, 2048);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    private static InetAddress fromIP(String ip) {
        ip = StringUtils.hasText(ip) ? ip : DEFAULT_IP;
        StringTokenizer st = new StringTokenizer(ip, ".");

        byte[] ipBytes = new byte[4];

        for (int i = 0; i < 4; i++) {
            ipBytes[i] = (byte) Integer.parseInt(st.nextToken());
        }
        try {
            return InetAddress.getByAddress(ipBytes);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
