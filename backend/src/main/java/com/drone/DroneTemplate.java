package com.drone;

import static com.drone.ReaderUtils.getFloat;
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
import java.util.Collections;
import java.util.List;
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
import com.drone.command.LandCommand;
import com.drone.command.MoveByAxisCommand;
import com.drone.command.ResetWatchdogCommand;
import com.drone.command.SetResetEmergencyCommand;
import com.drone.command.TakeOffCommand;

public class DroneTemplate implements Lifecycle {

    public static final String DEFAULT_IP = "192.168.1.1";

    public static final int COMMAND_PORT = 5556;
    public static final int NAV_PORT = 5554;

    // supported option tags
    private static final int CKS_TAG = -1;
    private static final int DEMO_TAG = 0;
    private static final int TIME_TAG = 1;
    private static final int RAW_MEASURES_TAG = 2;
    private static final int PHYS_MEASURES_TAG = 3;
    private static final int GYROS_OFFSETS_TAG = 4;
    private static final int EULER_ANGLES_TAG = 5;
    private static final int REFERENCES_TAG = 6;
    private static final int TRIMS_TAG = 7;
    private static final int RC_REFERENCES_TAG = 8;
    private static final int PWM_TAG = 9;
    private static final int ALTITUDE_TAG = 10;
    private static final int VISION_RAW_TAG = 11;
    private static final int VISION_OF_TAG = 12;
    private static final int VISION_TAG = 13;
    private static final int VISION_PERF_TAG = 14;
    private static final int TRACKERS_SEND_TAG = 15;
    private static final int VISION_DETECT_TAG = 16;
    private static final int WATCHDOG_TAG = 17;
    private static final int ADC_DATA_FRAME_TAG = 18;
    private static final int VIDEO_STREAM_TAG = 19;
    private static final int GAMES_TAG = 20;
    private static final int PRESSURE_RAW_TAG = 21;
    private static final int MAGNETO_TAG = 22;
    private static final int WIND_TAG = 23;
    private static final int KALMAN_PRESSURE_TAG = 24;
    private static final int HDVIDEO_STREAM_TAG = 25;
    private static final int WIFI_TAG = 26;
    private static final int ZIMMU_3000_TAG = 27;

    private volatile boolean running = false;
    private final PriorityBlockingQueue<DroneCommand> commands = new PriorityBlockingQueue<>(
            100, (l, r) -> l.getOrder() - r.getOrder());
    private InetAddress address = fromIP(DEFAULT_IP);
    private Logger logger = org.slf4j.LoggerFactory.getLogger(getClass());
    private final TaskExecutor taskExecutor;
    private final Object controlAckLock = new Object();
    private final AtomicInteger sequenceNumber = new AtomicInteger(1);
    private final AtomicBoolean controlAck = new AtomicBoolean();

    private List<DroneStateChangeCallback> callbacks = Collections.emptyList();

    public static interface ExceptionFriendlyRunnable {
        void run() throws Exception;
    }

    public DroneTemplate(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public DroneTemplate(TaskExecutor taskExecutor,
            DroneStateChangeCallback... callbacks) {
        this(taskExecutor);
        this.callbacks = Arrays.asList(callbacks);
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

        // parse options
        ByteBuffer b = buffer;
        int cks = 0;
        while (b.position() < b.limit()) {
            int tag = b.getShort() & 0xFFFF;
            int payloadSize = (b.getShort() & 0xFFFF) - 4;
            ByteBuffer optionData = b.slice().order(ByteOrder.LITTLE_ENDIAN);
            payloadSize = Math.min(payloadSize, optionData.remaining());
            optionData.limit(payloadSize);
            int cksOption = parseOption(tag, optionData, droneState);
            if (cksOption != 0)
                cks = cksOption;
            b.position(b.position() + payloadSize);
        }

        if (0 != cks)
            Assert.isTrue(ReaderUtils.getCRC(b, 0, b.limit() - 4) == cks,
                    "Checksum does not match");

        return droneState;
    }

    private float parseAltitudeOption(ByteBuffer b) {
        // if (altitudeListener.size() > 0) {
        int altitude_vision = b.getInt();

        float altitude_vz = b.getFloat();
        int altitude_ref = b.getInt();
        int altitude_raw = b.getInt();

        // TODO: what does obs mean?
        float obs_accZ = b.getFloat();
        float obs_alt = b.getFloat();

        float[] obs_x = getFloat(b, 3);

        int obs_state = b.getInt();

        // TODO: what does vb mean?
        float[] est_vb = getFloat(b, 2);

        int est_state = b.getInt();

        return altitude_vision;

    }

    private int parseOption(int tag, ByteBuffer optionData,
            DroneState droneState) {
        if (tag == WIFI_TAG) {
            // getUInt16( optionData) ; // tag
            // getUInt16( optionData) ; // size
            long linkQuality = getUInt32(optionData); //
            droneState.setLinkQuality((int) linkQuality);
        }

        if (tag == ALTITUDE_TAG) {
            droneState.setAltitude((int) this.parseAltitudeOption(optionData));
        }

        if (tag == DEMO_TAG) {
            int parsedTag = optionData.getShort();
            int parsedSize = optionData.getShort();
            int battery = optionData.getInt();
            droneState.setBattery(battery);

            float theta = optionData.getFloat();
            droneState.setTheta(theta);

            float phi = optionData.getFloat();
            droneState.setPhi(phi);

            float psi = optionData.getFloat();
            droneState.setPsi(psi);

            int altitude = optionData.getInt();
            droneState.setAltitude(altitude);
        }

        int checksum = 0;
        if (tag == CKS_TAG) {
            checksum = optionData.getInt();
            logger.info("checksum=" + checksum);
        }

        return checksum;
    }

    private void enqueueCommand(DroneCommand command) {
        commands.add(command);
        logger.info("size " + commands.size());
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
        navDataSocket.setSoTimeout(1000);

        // tickle!
        navDataSocket.send(new DatagramPacket(new byte[] { 0x01, 0x00, 0x00,
                0x00 }, 4, address, navPort));

        // get the deon
        bootstrapNavData(readDroneState(navDataSocket, maxPacketSize));

        enqueueCommand(new ConfigCommand("general:navdata_demo", false));
        enqueueCommand(new ConfigCommand("general:navdata_options",
                maskForOptions(ALTITUDE_TAG, CKS_TAG, DEMO_TAG, WIFI_TAG)));

        long lastReportTime = System.currentTimeMillis();

        while (isRunning()) {
            long timeNow = System.currentTimeMillis();
            DroneState currentState = readDroneState(navDataSocket,
                    maxPacketSize);

            if (timeNow - lastReportTime > 1000) {

                callbacks.forEach(cb -> cb.onDroneStateChanged(currentState));
                lastReportTime = timeNow;
            }

            /*
             * // detect control Ack change boolean newcontrolAck =
             * s.isControlReceived(); if (newcontrolAck != controlAck) {
             * manager.setControlAck(newcontrolAck); controlAck = newcontrolAck;
             * }
             * 
             * // TODO should we reset the communication watchdog always? if
             * (s.isCommunicationProblemOccurred()) {
             * manager.resetCommunicationWatchDog(); }
             * 
             * // TODO bootstrapping probably be handled by commandmanager if
             * (!bootstrapping && maskChanged) { manager.setNavDataDemo(false);
             * manager.setNavDataOptions(mask); maskChanged = false; }
             */
        }
    }

    private int maskForOptions(int... tags) {
        int mask = 0, newmask = 0;
        for (int tag : tags) {
            newmask |= 1 << tag;
        }
        mask |= newmask;
        return mask;
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
        String commandString = droneCommand.buildCommand(this.sequenceNumber
                .getAndIncrement());
        // logger.info(commandString);
        byte[] bytes = commandString.getBytes();
        datagramSocket.send(new DatagramPacket(bytes, bytes.length, address,
                commandPort));
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
                logger.info("Staring command loop executor");
                commandLoop(address, commandSocket, commandPort);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        runNavData(address, navPort, navSocket);
    }

    private void runNavData(InetAddress address, int navPort,
            DatagramSocket navSocket) {
        this.taskExecutor.execute(() -> {
            try {
                logger.info("Starting nav data executor");
                navDataLoop(address, navSocket, navPort, 2048);
            } catch (Exception e) {
                logger.warn("Rerunning nav data because of " + e.getMessage());
                runNavData(address, navPort, navSocket);
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

    public void takeoff() {
        enqueueCommand(new TakeOffCommand());
    }

    public void land() {
        enqueueCommand(new LandCommand());
    }

    public void setResetEmergency() {
        enqueueCommand(new SetResetEmergencyCommand());
    }

    public void move(float yaw, float pitch, float roll, float gaz) {
        System.out.println(yaw + " " + pitch + " " + roll + " " + gaz);
        enqueueCommand(new MoveByAxisCommand(pitch, roll, yaw, gaz, 0.5f));
    }
}